package com.fitifinance.comrade.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitifinance.comrade.data.entity.*
import com.fitifinance.comrade.engine.DrinkDatabase
import com.fitifinance.comrade.viewmodel.OnboardingViewModel
import com.fitifinance.comrade.viewmodel.fitiViewModel

@Composable
fun OnboardingWizard(onComplete: () -> Unit) {
    val vm: OnboardingViewModel = fitiViewModel()
    val profile by vm.profile.collectAsState()
    val step by vm.step.collectAsState()

    Scaffold(
        bottomBar = { OnboardingNavBar(vm, step, profile, onComplete) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            LinearProgressIndicator(
                progress = { step / vm.totalSteps.toFloat() },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
            Text("Step $step of ${vm.totalSteps}", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))

            when (step) {
                1 -> LivingSituationStep(profile, vm::setLivingSituation)
                2 -> RoommateStep(profile, vm::setRoommateStatus)
                3 -> MealStyleStep(profile, vm::setMealStyle)
                4 -> PartyProfilingStep(profile, vm)
            }
        }
    }
}

@Composable
private fun OnboardingNavBar(vm: OnboardingViewModel, step: Int, profile: UserProfile, onComplete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (step > 1) {
            OutlinedButton(onClick = { vm.previousStep() }) { Text("Back") }
        } else {
            Spacer(Modifier.width(1.dp))
        }

        if (step < vm.totalSteps) {
            Button(onClick = { vm.nextStep() }) { Text("Next") }
        } else {
            Button(onClick = { vm.completeOnboarding(onComplete) }) { Text("Build My Comrade Profile") }
        }
    }
}

@Composable
private fun StepHeader(title: String, subtitle: String) {
    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(4.dp))
    Text(subtitle, style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(20.dp))
}

@Composable
private fun ChoiceCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 6.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon, contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    title, style = MaterialTheme.typography.titleMedium,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    description, style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ---- Step 1: Living Situation ----
@Composable
private fun LivingSituationStep(profile: UserProfile, onSelect: (LivingSituation) -> Unit) {
    StepHeader("Where do you stay?", "This sets what food suggestions and cooking tools you'll see.")
    ChoiceCard(
        "In-Campus Hostel (No Cooking)", "Campus Mess, Kibanda & street snacks only",
        Icons.Filled.Apartment, profile.livingSituation == LivingSituation.IN_CAMPUS_NO_COOKING
    ) { onSelect(LivingSituation.IN_CAMPUS_NO_COOKING) }
    ChoiceCard(
        "In-Campus Hostel (Cooking Allowed)", "Single-burner / kettle recipes, snacks & Mess meals",
        Icons.Filled.Kitchen, profile.livingSituation == LivingSituation.IN_CAMPUS_COOKING_ALLOWED
    ) { onSelect(LivingSituation.IN_CAMPUS_COOKING_ALLOWED) }
    ChoiceCard(
        "Off-Campus Bedsitter / Single Room", "Full bulk cooking, weekly market groceries & Kibanda pricing",
        Icons.Filled.Home, profile.livingSituation == LivingSituation.OFF_CAMPUS_BEDSITTER
    ) { onSelect(LivingSituation.OFF_CAMPUS_BEDSITTER) }
}

// ---- Step 2: Roommates & Cost-Sharing ----
@Composable
private fun RoommateStep(profile: UserProfile, onSelect: (RoommateStatus) -> Unit) {
    StepHeader("Solo or with roommates?", "We'll enable shared expense logs if you're splitting costs.")
    ChoiceCard(
        "Solo", "Individual spending & saving targets",
        Icons.Filled.Person, profile.roommateStatus == RoommateStatus.SOLO
    ) { onSelect(RoommateStatus.SOLO) }
    ChoiceCard(
        "Living with Roommates", "Rent splits, gas refill contributions, bulk food buys",
        Icons.Filled.Groups, profile.roommateStatus == RoommateStatus.WITH_ROOMMATES
    ) { onSelect(RoommateStatus.WITH_ROOMMATES) }
}

// ---- Step 3: Meal Style ----
@Composable
private fun MealStyleStep(profile: UserProfile, onSelect: (MealStyle) -> Unit) {
    StepHeader("How do you usually eat?", "This tunes your default meal suggestions.")
    ChoiceCard(
        "Chef Comrade", "Weekly grocery budgeting & market price tracking",
        Icons.Filled.SoupKitchen, profile.mealStyle == MealStyle.CHEF_COMRADE
    ) { onSelect(MealStyle.CHEF_COMRADE) }
    ChoiceCard(
        "Kibanda Loyalist", "Local eatery price cards & fast quick-log tracking",
        Icons.Filled.Fastfood, profile.mealStyle == MealStyle.KIBANDA_LOYALIST
    ) { onSelect(MealStyle.KIBANDA_LOYALIST) }
}

// ---- Step 4: Party & Alcohol Profiling ----
@Composable
private fun PartyProfilingStep(profile: UserProfile, vm: OnboardingViewModel) {
    StepHeader("Do you go out?", "Selecting \"Never\" keeps your dashboard clean — no party module.")
    ChoiceCard("Regularly", "Party Dashboard fully enabled", Icons.Filled.Celebration, profile.partyStatus == PartyStatus.REGULARLY) {
        vm.setPartyStatus(PartyStatus.REGULARLY)
    }
    ChoiceCard("Occasionally", "Party Dashboard enabled, lighter defaults", Icons.Filled.LocalBar, profile.partyStatus == PartyStatus.OCCASIONALLY) {
        vm.setPartyStatus(PartyStatus.OCCASIONALLY)
    }
    ChoiceCard("Never", "Party Dashboard hidden entirely", Icons.Filled.Block, profile.partyStatus == PartyStatus.NEVER) {
        vm.setPartyStatus(PartyStatus.NEVER)
    }

    if (profile.partyStatus != PartyStatus.NEVER) {
        Spacer(Modifier.height(16.dp))
        Text("How often?", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                PartyFrequency.WEEKLY to "Weekly",
                PartyFrequency.BI_WEEKLY to "Bi-weekly",
                PartyFrequency.OCCASIONAL to "Occasional"
            ).forEach { (freq, label) ->
                FilterChip(
                    selected = profile.partyFrequency == freq,
                    onClick = { vm.setPartyFrequency(freq) },
                    label = { Text(label) }
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Favorite drinks", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        val favorites = profile.favoriteDrinkIds.split(",").map { it.trim() }.toSet()
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.height(240.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(DrinkDatabase.items) { drink ->
                FilterChip(
                    selected = favorites.contains(drink.id),
                    onClick = { vm.toggleFavoriteDrink(drink.id) },
                    label = { Text(drink.name) }
                )
            }
        }
    }
}
