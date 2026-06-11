package tr.erdaldemir.barem.ui.memur

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import tr.erdaldemir.barem.R
import tr.erdaldemir.barem.domain.model.HomeRoute
import tr.erdaldemir.barem.domain.model.MemurFormState
import tr.erdaldemir.barem.domain.model.StatKind
import tr.erdaldemir.barem.domain.model.StatisticsNavStore
import tr.erdaldemir.barem.ui.components.BaremDropdownField
import tr.erdaldemir.barem.ui.components.BaremIntGridPicker
import tr.erdaldemir.barem.ui.components.BaremSelectionCard
import tr.erdaldemir.barem.ui.components.BaremStringGridPicker
import tr.erdaldemir.barem.ui.components.BaremUnvanAutocompleteField
import tr.erdaldemir.barem.ui.premium.rememberEntitlement

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemurWizardScreen(
    onExit: () -> Unit,
    onOpenAnalytics: (HomeRoute) -> Unit = {},
    viewModel: MemurWizardViewModel = viewModel(),
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val step = uiState.step
    val form = uiState.form

    val entitlement = rememberEntitlement()
    val isPremium by entitlement.isPremium.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.consumeRestoreIntent()
    }

    LaunchedEffect(isPremium) {
        if (isPremium) {
            viewModel.refreshYearlySeriesIfNeeded()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.employee_memur))
                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (!viewModel.goBack()) onExit()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
        bottomBar = {
            if (step != MemurWizardStep.SONUC && step != MemurWizardStep.HIZMET_SINIFI) {
                val canContinue = when (step) {
                    MemurWizardStep.HIZMET_SINIFI -> form.hizmetSinifi != null
                    MemurWizardStep.KADRO -> {
                        form.unvan != null &&
                            form.derece != null &&
                            form.kademe != null &&
                            form.kidemYili != null &&
                            (!viewModel.showDetayField() || form.kadroDetay != null)
                    }
                    MemurWizardStep.KISISEL -> true
                    MemurWizardStep.SONUC -> true
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Button(
                        onClick = { viewModel.goNext() },
                        enabled = canContinue,
                    ) {
                        Text(stringResource(R.string.continue_btn))
                    }
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LinearProgressIndicator(
                progress = { (step.index + 1).toFloat() / MemurWizardStep.count },
                modifier = Modifier.fillMaxWidth(),
            )
            when (step) {
                MemurWizardStep.HIZMET_SINIFI -> HizmetSinifiStep(
                    codes = viewModel.hizmetSiniflari(),
                    selected = form.hizmetSinifi,
                    labelFor = viewModel::hizmetSinifiDisplay,
                    onSelect = { viewModel.selectHizmetSinifi(it) },
                )
                MemurWizardStep.KADRO -> KadroCascadeStep(
                    yearOptions = viewModel.hesapYillari(),
                    selectedYear = viewModel.effectiveHesapYili(),
                    searchUnvanlar = viewModel::searchUnvanlar,
                    detayOptions = viewModel.detaylar(),
                    showDetay = viewModel.showDetayField(),
                    dereceOptions = viewModel.dereceler(),
                    kademeOptions = viewModel.kademeSecenekleri(),
                    kidemOptions = viewModel.kidemSecenekleri(),
                    kidemLabelFor = viewModel::kidemDisplayLabel,
                    form = form,
                    onYear = { viewModel.selectHesapYili(it) },
                    onUnvan = { viewModel.selectUnvan(it) },
                    onDetay = { viewModel.selectKadroDetay(it) },
                    onDerece = { viewModel.selectDerece(it) },
                    onKademe = { viewModel.updateKademe(it) },
                    onKidem = { viewModel.updateKidemYili(it) },
                )
                MemurWizardStep.KISISEL -> KisiselStep(
                    form = form,
                    showIlIlce = viewModel.showIlIlceFields(),
                    showCocuk = viewModel.showCocukFields(),
                    medeniHalOptions = viewModel.medeniHalSecenekleri(),
                    sendikaOptions = viewModel.topluSozlesmeSecenekleri(),
                    sendikaLabel = viewModel.topluSozlesmeLabel(form.topluSozlesme),
                    dilSeviyeOptions = viewModel.yabanciDilSecenekleri(),
                    dilSeviyeLabel = viewModel.yabanciDilDisplay(form.yabanciDil),
                    ilOptions = viewModel.iller(),
                    ilceOptions = viewModel.ilceler(),
                    cocukOptions = viewModel.cocukSayiSecenekleri(),
                    onMedeniHal = { viewModel.selectMedeniHal(it) },
                    onSendika = { viewModel.updateTopluSozlesme(it) },
                    onDilSeviye = { viewModel.updateYabanciDil(it) },
                    onIl = { viewModel.selectIl(it) },
                    onIlce = { viewModel.selectIlce(it) },
                    onCocukUst6 = { viewModel.updateCocukUst6(it) },
                    onCocukAlt6 = { viewModel.updateCocukAlt6(it) },
                )
                MemurWizardStep.SONUC -> {
                    val result = uiState.salaryResult
                    if (result != null) {
                        ResultScreen(
                            result = result,
                            onOpenBordro = { onOpenAnalytics(HomeRoute.YearlyAverage) },
                            onOpenSalaryStats = {
                                StatisticsNavStore.selectedKind = StatKind.SALARY_TABLE
                                onOpenAnalytics(HomeRoute.StatSalaryTable)
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Column(Modifier.padding(padding)) {
                            EmptyStepMessage()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HizmetSinifiStep(
    codes: List<String>,
    selected: String?,
    labelFor: (String) -> String,
    onSelect: (String) -> Unit,
) {
    if (codes.isEmpty()) {
        EmptyStepMessage()
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.memur_sinif_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        items(codes) { code ->
            BaremSelectionCard(
                title = labelFor(code),
                selected = code == selected,
                onClick = { onSelect(code) },
                compact = true,
            )
        }
    }
}

@Composable
private fun KadroCascadeStep(
    yearOptions: List<Int>,
    selectedYear: Int,
    searchUnvanlar: (String) -> List<String>,
    detayOptions: List<String>,
    showDetay: Boolean,
    dereceOptions: List<Int>,
    kademeOptions: List<Int>,
    kidemOptions: List<Int>,
    kidemLabelFor: (Int) -> String,
    form: MemurFormState,
    onYear: (Int) -> Unit,
    onUnvan: (String) -> Unit,
    onDetay: (String) -> Unit,
    onDerece: (Int) -> Unit,
    onKademe: (Int) -> Unit,
    onKidem: (Int) -> Unit,
) {
    var yearExpanded by remember { mutableStateOf(false) }
    var detayExpanded by remember { mutableStateOf(false) }
    val placeholder = stringResource(R.string.dropdown_placeholder)
    val detayEmptyLabel = stringResource(R.string.detay_empty_label)
    val detayEnabled = form.unvan != null
    val unvanComplete = form.unvan != null &&
        (!showDetay || !form.kadroDetay.isNullOrBlank())
    val dereceEnabled = unvanComplete && dereceOptions.isNotEmpty()
    val showKademe = unvanComplete && form.derece != null && kademeOptions.isNotEmpty()
    val showKidem = form.kademe != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (yearOptions.isNotEmpty()) {
            BaremDropdownField(
                label = stringResource(R.string.field_hesap_yili),
                value = selectedYear.toString(),
                expanded = yearExpanded,
                onExpandedChange = { yearExpanded = it },
                options = yearOptions,
                optionLabel = { it.toString() },
                onSelect = onYear,
            )
        }
        BaremUnvanAutocompleteField(
            label = stringResource(R.string.field_unvan_only),
            selected = form.unvan,
            onSearch = searchUnvanlar,
            onSelect = onUnvan,
        )
        if (form.unvan != null && showDetay) {
            BaremDropdownField(
                label = stringResource(R.string.field_detay),
                value = when {
                    form.kadroDetay.isNullOrBlank() -> detayEmptyLabel
                    else -> form.kadroDetay!!
                },
                expanded = detayExpanded,
                onExpandedChange = { detayExpanded = it },
                options = detayOptions.filter { it.isNotBlank() },
                optionLabel = { it },
                onSelect = onDetay,
                enabled = detayEnabled,
            )
        }
        if (unvanComplete && dereceOptions.isNotEmpty()) {
            BaremIntGridPicker(
                title = stringResource(R.string.field_derece_select),
                options = dereceOptions,
                selected = form.derece,
                columns = minOf(4, dereceOptions.size.coerceAtLeast(1)),
                onSelect = onDerece,
                enabled = dereceEnabled,
            )
        }
        if (showKademe) {
            BaremIntGridPicker(
                title = stringResource(R.string.kademe),
                options = kademeOptions,
                selected = form.kademe,
                columns = minOf(3, kademeOptions.size.coerceAtLeast(1)),
                onSelect = onKademe,
            )
        }
        if (showKidem) {
            BaremIntGridPicker(
                title = stringResource(R.string.kidem_yili),
                options = kidemOptions,
                selected = form.kidemYili,
                columns = 5,
                labelFor = kidemLabelFor,
                onSelect = onKidem,
            )
        }
    }
}

@Composable
private fun KisiselStep(
    form: MemurFormState,
    showIlIlce: Boolean,
    showCocuk: Boolean,
    medeniHalOptions: List<String>,
    sendikaOptions: List<String>,
    sendikaLabel: String,
    dilSeviyeOptions: List<String>,
    dilSeviyeLabel: String,
    ilOptions: List<String>,
    ilceOptions: List<String>,
    cocukOptions: List<Int>,
    onMedeniHal: (String) -> Unit,
    onSendika: (String) -> Unit,
    onDilSeviye: (String) -> Unit,
    onIl: (String) -> Unit,
    onIlce: (String) -> Unit,
    onCocukUst6: (Int) -> Unit,
    onCocukAlt6: (Int) -> Unit,
) {
    var ilExpanded by remember { mutableStateOf(false) }
    var ilceExpanded by remember { mutableStateOf(false) }
    val placeholder = stringResource(R.string.dropdown_placeholder)
    val ilceEnabled = form.il != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        BaremStringGridPicker(
            title = stringResource(R.string.medeni_hal),
            options = medeniHalOptions,
            selected = form.medeniHal,
            columns = 1,
            onSelect = onMedeniHal,
            required = false,
        )
        if (showCocuk) {
            BaremIntGridPicker(
                title = stringResource(R.string.cocuk_ust_6),
                options = cocukOptions,
                selected = form.cocukUst6,
                columns = 6,
                onSelect = onCocukUst6,
                required = false,
            )
            BaremIntGridPicker(
                title = stringResource(R.string.cocuk_alt_6),
                options = cocukOptions,
                selected = form.cocukAlt6,
                columns = 6,
                onSelect = onCocukAlt6,
                required = false,
            )
        }
        BaremStringGridPicker(
            title = stringResource(R.string.field_sendika_var_mi),
            options = sendikaOptions,
            selected = sendikaLabel,
            columns = 2,
            onSelect = onSendika,
            required = false,
        )
        BaremStringGridPicker(
            title = stringResource(R.string.field_yabanci_dil_seviye),
            options = dilSeviyeOptions,
            selected = dilSeviyeLabel,
            columns = 3,
            onSelect = onDilSeviye,
            required = false,
        )
        if (showIlIlce) {
            BaremDropdownField(
                label = stringResource(R.string.field_il),
                value = form.il ?: placeholder,
                expanded = ilExpanded,
                onExpandedChange = { ilExpanded = it },
                options = ilOptions,
                optionLabel = { it },
                onSelect = onIl,
                required = false,
            )
            BaremDropdownField(
                label = stringResource(R.string.field_ilce),
                value = form.ilce ?: placeholder,
                expanded = ilceExpanded,
                onExpandedChange = { ilceExpanded = it },
                options = ilceOptions,
                optionLabel = { it },
                onSelect = onIlce,
                enabled = ilceEnabled,
                required = false,
            )
        }
        if (!form.bolgeselKod.isNullOrBlank()) {
            Text(
                text = "${stringResource(R.string.field_bolgesel_kod)}: ${form.bolgeselKod}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyStepMessage() {
    Text(
        text = stringResource(R.string.empty_step_message),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(24.dp),
    )
}
