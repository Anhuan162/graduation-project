package com.graduation.project.auth.repository;

import com.graduation.project.common.entity.OauthAccount;
import com.graduation.project.common.entity.Provider;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface OauthAccountRepository extends CrudRepository<OauthAccount, Long> {
    Optional<OauthAccount> findByProviderAndProviderUserId(Provider provider, String providerUserId);
}
