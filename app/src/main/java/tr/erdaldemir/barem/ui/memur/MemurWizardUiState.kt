package tr.erdaldemir.barem.ui.memur

import tr.erdaldemir.barem.domain.calc.YearlyCalcEngine
import tr.erdaldemir.barem.domain.model.MemurFormState
import tr.erdaldemir.barem.domain.model.SalaryResult

data class MemurWizardUiState(
    val step: MemurWizardStep = MemurWizardStep.HIZMET_SINIFI,
    val form: MemurFormState = MemurFormState(),
    val salaryResult: SalaryResult? = null,
    val yearlySeries: List<YearlyCalcEngine.YearlyCalcRow> = emptyList(),
)