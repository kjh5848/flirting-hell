package com.flirtinghell.identity.adapter.out.firebase;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FirebaseAdminCredentialSourceTest {

	@Test
	void resolvesBase64ServiceAccountBeforeFilePath() {
		String serviceAccountJson = """
				{"type":"service_account","project_id":"flirting-hell"}
				""";
		String encodedServiceAccount = Base64.getEncoder()
				.encodeToString(serviceAccountJson.getBytes(StandardCharsets.UTF_8));
		MockEnvironment environment = new MockEnvironment()
				.withProperty("flirting-hell.firebase.service-account-path", "/tmp/should-not-be-used.json")
				.withProperty("flirting-hell.firebase.service-account-base64", encodedServiceAccount);

		FirebaseAdminCredentialSource source = FirebaseAdminCredentialSource.resolve(environment);

		assertThat(source.kind()).isEqualTo(FirebaseAdminCredentialSource.Kind.BASE64_SERVICE_ACCOUNT);
		assertThat(source.readServiceAccountBytes()).isEqualTo(serviceAccountJson.getBytes(StandardCharsets.UTF_8));
	}

	@Test
	void rejectsInvalidBase64ServiceAccount() {
		MockEnvironment environment = new MockEnvironment()
				.withProperty("flirting-hell.firebase.service-account-base64", "not base64");

		FirebaseAdminCredentialSource source = FirebaseAdminCredentialSource.resolve(environment);

		assertThatThrownBy(source::readServiceAccountBytes)
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Invalid Firebase service account base64");
	}
}
