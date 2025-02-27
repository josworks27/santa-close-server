package com.santaclose.app.auth.service

import arrow.core.left
import arrow.core.right
import com.navercorp.fixturemonkey.kotlin.KFixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.santaclose.app.appUser.repository.AppUserAppQueryRepository
import com.santaclose.app.appUser.repository.AppUserAppRepository
import com.santaclose.app.auth.context.AppSession
import com.santaclose.lib.auth.Profile
import com.santaclose.lib.entity.appUser.AppUser
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class AuthAppServiceTest {
  private val appUserAppQueryRepository = mockk<AppUserAppQueryRepository>()
  private val appUserAppRepository = mockk<AppUserAppRepository>()
  private val authManager = mockk<AuthManager>()
  private val sut = KFixtureMonkey.create()

  private val authAppService =
    AuthAppService(appUserAppQueryRepository, appUserAppRepository, authManager)

  @Nested
  inner class GetProfile {
    @Test
    fun `프로필 정보를 가져오지 못한경우 left 반환한다`() = runTest {
      // given
      val code = "code"
      val exception = Exception("profile error")
      coEvery { authManager.getProfile(code) } returns exception.left()

      // when
      val result = authAppService.signIn(code)

      // then
      result shouldBeLeft exception
    }

    @Test
    fun `기존에 존재하는 유저가 있다면 세션 정보를 반환한다`() = runTest {
      // given
      val code = "code"
      val profile = sut.giveMeOne<Profile>()
      coEvery { authManager.getProfile(code) } returns profile.right()
      val appUser = sut.giveMeOne<AppUser>()
      coEvery { appUserAppQueryRepository.findBySocialId(profile.id) } returns appUser.right()

      // when
      val result = authAppService.signIn(code)

      // then
      result shouldBeRight AppSession(appUser.id, appUser.role)
    }

    @Test
    fun `신규 유저의 프로필을 생성 한 후 세션 정보를 반환한다`() = runTest {
      // given
      val code = "code"
      val profile = sut.giveMeOne<Profile>()
      coEvery { authManager.getProfile(code) } returns profile.right()
      coEvery { appUserAppQueryRepository.findBySocialId(profile.id) } returns null.right()
      val appUser = AppUser.signUp(profile.name, profile.email, profile.id)
      coEvery { appUserAppRepository.save(any()) } returns appUser

      // when
      val result = authAppService.signIn(code)

      // then
      result shouldBeRight AppSession(appUser.id, appUser.role)
    }
  }
}
