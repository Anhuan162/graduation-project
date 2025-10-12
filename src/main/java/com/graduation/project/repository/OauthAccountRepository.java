package com.graduation.project.repository;

import com.graduation.project.entity.OauthAccount;
import com.graduation.project.entity.Provider;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface OauthAccountRepository extends CrudRepository<OauthAccount, Long> {
  Optional<OauthAccount> findByProviderAndProviderUserId(Provider provider, String providerUserId);
}
