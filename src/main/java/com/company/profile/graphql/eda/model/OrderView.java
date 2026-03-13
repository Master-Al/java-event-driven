package ccom.company.profile.graphql.eda.model;

public record OrderView(
        String orderId,
        String status,
        String lastUpdated
) {
}
