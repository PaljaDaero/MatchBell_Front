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
public final class SignupInfoViewModel_Factory implements Factory<SignupInfoViewModel> {
  private final Provider<AuthApi> authApiProvider;

  public SignupInfoViewModel_Factory(Provider<AuthApi> authApiProvider) {
    this.authApiProvider = authApiProvider;
  }

  @Override
  public SignupInfoViewModel get() {
    return newInstance(authApiProvider.get());
  }

  public static SignupInfoViewModel_Factory create(Provider<AuthApi> authApiProvider) {
    return new SignupInfoViewModel_Factory(authApiProvider);
  }

  public static SignupInfoViewModel newInstance(AuthApi authApi) {
    return new SignupInfoViewModel(authApi);
  }
}
