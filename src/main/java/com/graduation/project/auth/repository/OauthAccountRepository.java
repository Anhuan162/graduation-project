package com.graduation.project.auth.repository;

import com.graduation.project.common.entity.OauthAccount;
import com.graduation.project.common.entity.Provider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OauthAccountRepository extends JpaRepository<OauthAccount, Long> {
  Optional<OauthAccount> findByProviderAndProviderUserId(Provider provider, String providerUserId);
}
