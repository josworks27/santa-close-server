package com.santaclose.app.mountain.service

import com.expediagroup.graphql.generator.scalars.ID
import com.santaclose.app.mountain.repository.MountainAppQueryRepository
import com.santaclose.app.mountain.resolver.dto.MountainAppDetail
import com.santaclose.app.mountainReview.repository.MountainReviewAppQueryRepository
import com.santaclose.lib.web.toLong
import org.springframework.stereotype.Service

@Service
class MountainAppQueryService(
  private val mountainAppQueryRepository: MountainAppQueryRepository,
  private val mountainReviewAppQueryRepository: MountainReviewAppQueryRepository,
) {
  fun findDetail(id: ID): MountainAppDetail {
    val mountain = mountainAppQueryRepository.findOneWithLocation(id.toLong())
    val mountainReviews = mountainReviewAppQueryRepository.findAllByMountainId(mountain.id, 5)
    val mountainRatingAverageDto =
      mountainReviewAppQueryRepository.findMountainRatingAverages(mountain.id)
    // 음식점 추가 예정

    return MountainAppDetail(
      mountain.name,
      mountain.location.address,
      mountainReviews,
      mountainRatingAverageDto
    )
  }
}
