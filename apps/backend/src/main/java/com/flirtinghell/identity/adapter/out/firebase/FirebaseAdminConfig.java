package com.flirtinghell.identity.adapter.out.firebase;

import java.io.IOException;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Configuration
@Profile("firebase")
class FirebaseAdminConfig {

	@Bean
	FirebaseApp firebaseApp(Environment environment) throws IOException {
		try {
			return FirebaseApp.getInstance();
		} catch (IllegalStateException exception) {
			FirebaseOptions.Builder options = FirebaseOptions.builder()
					.setCredentials(resolveCredentials(environment));
			String projectId = environment.getProperty("flirting-hell.firebase.project-id");
			if (projectId != null && !projectId.isBlank()) {
				options.setProjectId(projectId);
			}
			return FirebaseApp.initializeApp(options.build());
		}
	}

	@Bean
	FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
		return FirebaseAuth.getInstance(firebaseApp);
	}

	private GoogleCredentials resolveCredentials(Environment environment) throws IOException {
		return FirebaseAdminCredentialSource.resolve(environment).credentials();
	}
}
