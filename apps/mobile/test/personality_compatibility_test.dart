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
