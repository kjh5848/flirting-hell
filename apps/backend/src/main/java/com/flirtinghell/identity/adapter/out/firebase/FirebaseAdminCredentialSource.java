package com.flirtinghell.identity.adapter.out.firebase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.core.env.Environment;

final class FirebaseAdminCredentialSource {

	private static final String SERVICE_ACCOUNT_BASE64_PROPERTY = "flirting-hell.firebase.service-account-base64";
	private static final String SERVICE_ACCOUNT_PATH_PROPERTY = "flirting-hell.firebase.service-account-path";

	private final Kind kind;
	private final String value;

	private FirebaseAdminCredentialSource(Kind kind, String value) {
		this.kind = kind;
		this.value = value;
	}

	static FirebaseAdminCredentialSource resolve(Environment environment) {
		String serviceAccountBase64 = environment.getProperty(SERVICE_ACCOUNT_BASE64_PROPERTY);
		if (serviceAccountBase64 != null && !serviceAccountBase64.isBlank()) {
			return new FirebaseAdminCredentialSource(Kind.BASE64_SERVICE_ACCOUNT, serviceAccountBase64);
		}

		String serviceAccountPath = environment.getProperty(SERVICE_ACCOUNT_PATH_PROPERTY);
		if (serviceAccountPath != null && !serviceAccountPath.isBlank()) {
			return new FirebaseAdminCredentialSource(Kind.FILE_SERVICE_ACCOUNT, serviceAccountPath);
		}

		return new FirebaseAdminCredentialSource(Kind.APPLICATION_DEFAULT, null);
	}

	Kind kind() {
		return kind;
	}

	byte[] readServiceAccountBytes() {
		return switch (kind) {
			case BASE64_SERVICE_ACCOUNT -> decodeBase64ServiceAccount(value);
			case FILE_SERVICE_ACCOUNT -> readServiceAccountFile(value);
			case APPLICATION_DEFAULT -> throw new IllegalStateException("Application default credentials do not use a service account file.");
		};
	}

	GoogleCredentials credentials() throws IOException {
		if (kind == Kind.APPLICATION_DEFAULT) {
			return GoogleCredentials.getApplicationDefault();
		}

		return GoogleCredentials.fromStream(new ByteArrayInputStream(readServiceAccountBytes()));
	}

	private static byte[] decodeBase64ServiceAccount(String encodedServiceAccount) {
		try {
			return Base64.getDecoder().decode(encodedServiceAccount);
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Invalid Firebase service account base64.", exception);
		}
	}

	private static byte[] readServiceAccountFile(String serviceAccountPath) {
		try {
			return Files.readAllBytes(Path.of(serviceAccountPath));
		} catch (IOException exception) {
			throw new IllegalArgumentException("Firebase service account file cannot be read.", exception);
		}
	}

	enum Kind {
		BASE64_SERVICE_ACCOUNT,
		FILE_SERVICE_ACCOUNT,
		APPLICATION_DEFAULT
	}
}
