package com.wardellbagby.deltas.android.trackers.creation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.TextController
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen
import com.squareup.workflow1.ui.compose.asMutableState
import com.wardellbagby.deltas.android.R
import com.wardellbagby.deltas.android.core_ui.LabeledRadioButton
import com.wardellbagby.deltas.android.strings.isNotNullOrBlank
import com.wardellbagby.deltas.models.trackers.TrackerType
import com.wardellbagby.deltas.models.trackers.TrackerType.Elapsed
import com.wardellbagby.deltas.models.trackers.TrackerType.Incremental
import com.wardellbagby.deltas.models.trackers.TrackerVisibility
import com.wardellbagby.deltas.models.trackers.TrackerVisibility.Private
import com.wardellbagby.deltas.models.trackers.TrackerVisibility.Public

data class CreateTrackerScreen(
  val type: TrackerType,
  val visibility: TrackerVisibility,
  val labelTextController: TextController,
  val onTypeSelected: (type: TrackerType) -> Unit,
  val onVisibilitySelected: (visibility: TrackerVisibility) -> Unit,
  val onBack: () -> Unit,
  val onSave: () -> Unit
) : ComposeScreen {

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content(viewEnvironment: ViewEnvironment) {
    var label by labelTextController.asMutableState()
    val focusRequester = remember {
      FocusRequester()
    }

    BackHandler {
      onBack()
    }

    LaunchedEffect(Unit) {
      focusRequester.requestFocus()
    }

    Column(
      Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)
        .padding(bottom = 16.dp)
    ) {
      Column(
        Modifier
          .weight(1f)
          .verticalScroll(rememberScrollState())
      ) {
        OutlinedTextField(
          modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
          label = { Text(stringResource(R.string.label)) },
          value = label,
          onValueChange = { label = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LabeledRadioGroup(
          label = stringResource(R.string.what_should_this_track),
          options = listOf(
            LabeledOption(
              label = stringResource(R.string.time_since_reset),
              option = Elapsed
            ),
            LabeledOption(
              label = stringResource(R.string.total_action_count),
              option = Incremental
            )
          ),
          selectedOption = type,
          onOptionSelected = onTypeSelected
        )

        Spacer(modifier = Modifier.height(16.dp))

        LabeledRadioGroup(
          label = stringResource(R.string.tracker_visibility),
          options = listOf(
            LabeledOption(
              label = stringResource(R.string.private_tracker_visibility),
              subLabel = stringResource(R.string.private_tracker_visibility_explanation),
              option = Private
            ),
            LabeledOption(
              label = stringResource(R.string.public_tracker_visibility),
              subLabel = stringResource(R.string.public_tracker_visibility_explanation),
              option = Public
            )
          ),
          selectedOption = visibility,
          onOptionSelected = onVisibilitySelected
        )
      }

      Button(
        modifier = Modifier.fillMaxWidth(),
        enabled = label.isNotNullOrBlank(),
        onClick = {
          onSave()
        }) {
        Text(text = stringResource(R.string.save))
      }
    }
  }

  data class LabeledOption<T>(val label: String, val subLabel: String? = null, val option: T)

  @Composable
  private fun <T> LabeledRadioGroup(
    label: String,
    options: List<LabeledOption<T>>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit
  ) {
    Column(Modifier.fillMaxWidth()) {
      Text(
        label,
        style = MaterialTheme.typography.labelMedium
      )
      Spacer(modifier = Modifier.height(8.dp))

      options.forEach { (optionLabel, subLabel, option) ->
        LabeledRadioButton(
          modifier = Modifier.fillMaxWidth(),
          label = optionLabel,
          subLabel = subLabel,
          selected = option == selectedOption,
          onClick = { onOptionSelected(option) })
      }
    }
  }
}