package tr.erdaldemir.barem

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import tr.erdaldemir.barem.ui.memur.MemurWizardStep
import tr.erdaldemir.barem.ui.memur.MemurWizardViewModel
import tr.erdaldemir.barem.ui.memur.ResultCurrency
import tr.erdaldemir.barem.ui.memur.ResultCurrencyData
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
class MemurWizardEngineSmokeTest {

    @Test
    fun yhsHizmetliFlow_reachesGoldenNetValues() {
        val app = ApplicationProvider.getApplicationContext<BaremApplication>()
        val vm = MemurWizardViewModel(app)

        vm.selectHizmetSinifi("YHS")
        assertEquals(MemurWizardStep.KADRO, vm.state.value.step)

        vm.selectUnvan("HİZMETLİ")
        if (vm.showDetayField()) {
            vm.selectKadroDetay("Lisans")
        }
        vm.selectDerece(1)
        vm.updateKademe(4)
        vm.updateKidemYili(0)

        assertTrue(vm.canContinue())
        assertTrue(vm.goNext())
        assertEquals(MemurWizardStep.KISISEL, vm.state.value.step)

        assertTrue(vm.goNext())

        val result = waitForSalaryResult(vm)
        assertEquals(MemurWizardStep.SONUC, vm.state.value.step)
        assertEquals(12, result.monthlyNet.size)
        assertClose(62_189.25, result.monthlyNet.first(), 1.0)
        assertClose(66_524.95, result.monthlyNet[6], 1.0)
        assertClose(72_574.48, result.brutAylik, 1.0)
        assertNotNull(result.fxOutput)

        val usdMonthly = ResultCurrencyData.monthlyValues(result, ResultCurrency.USD)
        assertEquals(12, usdMonthly.size)
        assertTrue(usdMonthly.all { it > 0 })
    }

    private fun waitForSalaryResult(
        vm: MemurWizardViewModel,
        timeoutMs: Long = 15_000,
    ): tr.erdaldemir.barem.domain.model.SalaryResult {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            vm.state.value.salaryResult?.let { return it }
            Thread.sleep(50)
        }
        error("Salary result not produced within ${timeoutMs}ms")
    }

    private fun assertClose(expected: Double, actual: Double, tolerance: Double) {
        assertTrue(
            "expected $expected but was $actual (tol=$tolerance)",
            abs(expected - actual) <= tolerance,
        )
    }
}
