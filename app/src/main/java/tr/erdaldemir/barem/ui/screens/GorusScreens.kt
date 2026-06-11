package tr.erdaldemir.barem.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tr.erdaldemir.barem.R
import tr.erdaldemir.barem.domain.model.GorusCatalog
import tr.erdaldemir.barem.domain.model.GorusCategory
import tr.erdaldemir.barem.domain.model.GorusNavStore
import tr.erdaldemir.barem.domain.model.GorusTopic
import tr.erdaldemir.barem.domain.model.HomeRoute
import tr.erdaldemir.barem.domain.model.HomeTile
import tr.erdaldemir.barem.ui.components.BaremHomePrimaryCard

@Composable
fun GorusHubScreen(
    onBack: () -> Unit,
    onOpenCategory: (HomeRoute) -> Unit,
) {
    SendikaFeatureScaffold(
        title = stringResource(R.string.nav_gorus),
        onBack = onBack,
        scrollable = true,
    ) {
        Text(
            text = stringResource(R.string.gorus_hub_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        GorusCategoryGrid(
            categories = GorusCatalog.categories,
            onCategoryClick = { category ->
                GorusNavStore.categoryId = category.id
                onOpenCategory(HomeRoute.GorusCategory)
            },
        )
    }
}

@Composable
fun GorusCategoryScreen(
    onBack: () -> Unit,
    onOpenTopic: (HomeRoute) -> Unit,
) {
    val category = GorusCatalog.category(GorusNavStore.categoryId.orEmpty())
    if (category == null) {
        SendikaFeatureScaffold(title = stringResource(R.string.nav_gorus), onBack = onBack) {
            Text(stringResource(R.string.empty_step_message))
        }
        return
    }

    SendikaFeatureScaffold(
        title = stringResource(category.titleRes),
        onBack = onBack,
        scrollable = true,
    ) {
        if (category.topics.isEmpty()) {
            Text(
                text = stringResource(R.string.gorus_coming_soon),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@SendikaFeatureScaffold
        }
        Text(
            text = stringResource(R.string.gorus_category_topics_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        GorusTopicGrid(
            topics = category.topics,
            style = category.style,
            onTopicClick = { topic ->
                GorusNavStore.topicId = topic.id
                onOpenTopic(HomeRoute.GorusArticle)
            },
        )
    }
}

@Composable
fun GorusArticleScreen(onBack: () -> Unit) {
    val category = GorusCatalog.category(GorusNavStore.categoryId.orEmpty())
    val topic = category?.let { GorusCatalog.topic(it.id, GorusNavStore.topicId.orEmpty()) }

    SendikaFeatureScaffold(
        title = topic?.let { stringResource(it.titleRes) } ?: stringResource(R.string.nav_gorus),
        onBack = onBack,
    ) {
        Text(
            text = stringResource(R.string.gorus_article_placeholder),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

/** Ana sayfa ile aynı 2 sütunlu kutucuk düzeni — LazyGrid kullanılmaz (çökme önlenir). */
@Composable
private fun GorusCategoryGrid(
    categories: List<GorusCategory>,
    onCategoryClick: (GorusCategory) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        categories.chunked(HomeTile.PRIMARY_COLUMNS).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                row.forEach { category ->
                    BaremHomePrimaryCard(
                        title = stringResource(category.titleRes),
                        subtitle = stringResource(R.string.gorus_cat_sub),
                        style = category.style,
                        enabled = true,
                        onClick = { onCategoryClick(category) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(HomeTile.PRIMARY_COLUMNS - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun GorusTopicGrid(
    topics: List<GorusTopic>,
    style: tr.erdaldemir.barem.domain.model.HomeTileStyle,
    onTopicClick: (GorusTopic) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        topics.chunked(HomeTile.PRIMARY_COLUMNS).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                row.forEach { topic ->
                    BaremHomePrimaryCard(
                        title = stringResource(topic.titleRes),
                        subtitle = stringResource(R.string.gorus_topic_sub),
                        style = style,
                        enabled = true,
                        onClick = { onTopicClick(topic) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(HomeTile.PRIMARY_COLUMNS - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
