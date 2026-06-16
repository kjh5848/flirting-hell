import 'package:flirting_hell/features/personality/domain/personality_axes.dart';
import 'package:flirting_hell/features/personality/domain/personality_models.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  group('computeCompatibility', () {
    test('identical ideal and partner scores 100', () {
      final scores = {for (final axis in personalityAxes) axis.id: 3};
      final result = computeCompatibility(ideal: scores, partner: scores);
      expect(result.score, 100);
      expect(result.biggestGap.gap, 0);
    });

    test('maximum distance on every axis scores 0', () {
      final ideal = {for (final axis in personalityAxes) axis.id: 1};
      final partner = {for (final axis in personalityAxes) axis.id: 5};
      final result = computeCompatibility(ideal: ideal, partner: partner);
      expect(result.score, 0);
      expect(result.biggestGap.gap, 4);
    });

    test('half-distance on every axis scores around 50', () {
      final ideal = {for (final axis in personalityAxes) axis.id: 1};
      final partner = {for (final axis in personalityAxes) axis.id: 3};
      final result = computeCompatibility(ideal: ideal, partner: partner);
      // gap 2 of max 4 on every axis -> 50%.
      expect(result.score, 50);
    });

    test('missing axis values fall back to neutral without throwing', () {
      final result = computeCompatibility(
        ideal: const {'expression': 5},
        partner: const {},
      );
      expect(result.perAxis.length, personalityAxes.length);
      expect(result.score, inInclusiveRange(0, 100));
    });

    test('best match and biggest gap point at the right axes', () {
      final result = computeCompatibility(
        ideal: const {
          'expression': 5,
          'pace': 1,
          'contact': 3,
          'emotion': 3,
          'values': 3,
        },
        partner: const {
          'expression': 5, // gap 0 -> best
          'pace': 5, // gap 4 -> biggest
          'contact': 3,
          'emotion': 3,
          'values': 3,
        },
      );
      expect(result.bestMatch.axis.id, 'expression');
      expect(result.biggestGap.axis.id, 'pace');
    });
  });

  group('PersonalityProfile storage', () {
    test('fromStored falls back to neutral on null/empty/garbage', () {
      for (final raw in [null, '', '   ', 'not json', '{']) {
        final p = PersonalityProfile.fromStored(self: raw, ideal: raw);
        expect(p.self.length, personalityAxes.length);
        expect(p.self.values.every((v) => v == 3), isTrue);
      }
    });

    test('JSON round-trips and clamps out-of-range values', () {
      final original = PersonalityProfile(
        self: {for (final a in personalityAxes) a.id: 5},
        ideal: {for (final a in personalityAxes) a.id: 1},
      );
      final restored = PersonalityProfile.fromStored(
        self: original.selfJson,
        ideal: original.idealJson,
      );
      expect(restored.self, original.self);
      expect(restored.ideal, original.ideal);

      final clamped = PersonalityProfile.fromStored(
        self: '{"expression": 99, "pace": -3}',
        ideal: '{}',
      );
      expect(clamped.self['expression'], personalityScoreMax);
      expect(clamped.self['pace'], personalityScoreMin);
    });
  });

  test('scores stay within 1..5 via clamping', () {
    final result = computeCompatibility(
      ideal: const {'expression': 99},
      partner: const {'expression': -10},
    );
    final expr = result.perAxis.firstWhere((g) => g.axis.id == 'expression');
    expect(expr.idealValue, personalityScoreMax);
    expect(expr.partnerValue, personalityScoreMin);
  });
}
