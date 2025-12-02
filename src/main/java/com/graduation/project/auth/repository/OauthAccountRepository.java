package com.graduation.project.auth.repository;

import com.graduation.project.common.entity.OauthAccount;
import com.graduation.project.common.constant.Provider;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OauthAccountRepository extends JpaRepository<OauthAccount, UUID> {
  Optional<OauthAccount> findByProviderAndProviderUserId(Provider provider, String providerUserId);
}
