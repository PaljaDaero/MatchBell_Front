package com.example.matchbell.feature.auth;

import com.example.matchbell.network.AuthApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class LoginViewModel_Factory implements Factory<LoginViewModel> {
  private final Provider<AuthApi> authApiProvider;

  public LoginViewModel_Factory(Provider<AuthApi> authApiProvider) {
    this.authApiProvider = authApiProvider;
  }

  @Override
  public LoginViewModel get() {
    return newInstance(authApiProvider.get());
  }

  public static LoginViewModel_Factory create(Provider<AuthApi> authApiProvider) {
    return new LoginViewModel_Factory(authApiProvider);
  }

  public static LoginViewModel newInstance(AuthApi authApi) {
    return new LoginViewModel(authApi);
  }
}
