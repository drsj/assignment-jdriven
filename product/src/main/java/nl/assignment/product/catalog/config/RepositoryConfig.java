package nl.assignment.product.catalog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "nl.assignment.product.catalog.repository")
@EnableElasticsearchRepositories(basePackages = "nl.assignment.product.catalog.search.repository")
public class RepositoryConfig {
}

