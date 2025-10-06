package com.graduation.project.repository;

import com.graduation.project.entity.OauthAccount;
import com.graduation.project.entity.Provider;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface OauthAccountRepository extends CrudRepository<OauthAccount, Long> {
    Optional<OauthAccount> findByProviderAndProviderUserId(Provider provider, String providerUserId);
}
