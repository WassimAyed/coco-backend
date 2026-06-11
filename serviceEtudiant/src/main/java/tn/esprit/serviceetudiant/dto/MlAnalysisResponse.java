package tn.esprit.serviceetudiant.dto;

import java.util.List;

/**
 * Subset of the ML service response that the backend actually uses.
 * Full schema: machine_learning/models/schemas.py → AnalyzeResponse.
 *
 * decision values: "APPROVED" | "NEEDS_REVIEW" | "REJECTED"
 */
public record MlAnalysisResponse(
        String decision,
        double confidence,
        List<String> flags
) {}
