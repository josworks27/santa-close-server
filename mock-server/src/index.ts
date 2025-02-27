import { gql } from 'apollo-server';
import { readFileSync } from 'fs';
import { join } from 'path';
import { mocks } from './mocks';
import express from 'express';
import http from 'http';
import { ApolloServer } from 'apollo-server-express';
import faker from '@faker-js/faker';

async function startApolloServer() {
  const app = express();
  const httpServer = http.createServer(app);

  const typeDefs = gql`
    ${readFileSync(
      join(__dirname, '../../app/src/main/resources/graphql/schema.graphql')
    )}
  `;
  const server = new ApolloServer({
    typeDefs,
    mocks,
  });
  await server.start();
  server.applyMiddleware({ app });

  app.post('/api/image', (req, res) =>
    res.json({ url: faker.image.imageUrl() })
  );

  await new Promise<void>((resolve) =>
    httpServer.listen({ port: 4000 }, resolve)
  );
  console.log(`🚀 Server ready at http://localhost:4000${server.graphqlPath}`);
}

startApolloServer().catch(console.error);
