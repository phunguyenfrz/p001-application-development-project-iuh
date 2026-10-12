package com.petshop.search;

import com.petshop.model.Role;
import com.petshop.model.Screen;
import com.petshop.model.SearchResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalSearchServiceTest {
    @Test
    void returnsBothCustomersForMixedCaseAndPartialGlobalSearches() {
        SearchProvider customers = new StubProvider(
                "Khách hàng", Set.of(Role.BAN_HANG),
                List.of(
                        new SearchResult("Khách hàng", "KH-1", "Hoangl Nguyen",
                                "0900000001", Screen.CUSTOMERS),
                        new SearchResult("Khách hàng", "KH-2", "Hoangl Tran",
                                "0900000002", Screen.CUSTOMERS),
                        new SearchResult("Khách hàng", "KH-3", "Nguyễn Văn A",
                                "0900000003", Screen.CUSTOMERS)));
        GlobalSearchService service = new GlobalSearchService(List.of(customers));

        for (String query : List.of("Hoangl", "hoangl", "HOANGL", "Hoa")) {
            assertEquals(List.of("KH-1", "KH-2"), service.search(query, Role.BAN_HANG)
                    .stream().map(SearchResult::id).toList(), query);
        }
    }

    @Test
    void returnsAllSubstringMatchesWithoutCollapsingDuplicateNames() {
        SearchProvider customers = new StubProvider(
                "Khách hàng", Set.of(Role.BAN_HANG, Role.CHAM_SOC),
                List.of(
                        new SearchResult("Khách hàng", "KH-1", "Nguyễn Thị Lan",
                                "0900000001", Screen.CUSTOMERS),
                        new SearchResult("Khách hàng", "KH-2", "Nguyễn Thị Lan",
                                "0900000002", Screen.CUSTOMERS),
                        new SearchResult("Khách hàng", "KH-3", "Hùng",
                                "0900000003", Screen.CUSTOMERS)));
        SearchProvider pets = new StubProvider(
                "Thú cưng", Set.of(Role.CHAM_SOC),
                List.of(new SearchResult("Thú cưng", "TC-1", "Milo",
                        "Khách hàng: Lan Nguyễn", Screen.PETS)));
        GlobalSearchService service = new GlobalSearchService(List.of(customers, pets));

        assertEquals(List.of("KH-1", "KH-2"), service.search("lan", Role.BAN_HANG)
                .stream().map(SearchResult::id).toList());
        assertEquals(List.of("KH-1", "KH-2"), service.search("LAN", Role.BAN_HANG)
                .stream().map(SearchResult::id).toList());
        assertEquals(List.of("KH-1", "KH-2", "TC-1"),
                service.search("nguyen lan", Role.CHAM_SOC)
                        .stream().map(SearchResult::id).toList());
        assertEquals(List.of(), service.search(" ", Role.CHAM_SOC));
    }

    private record StubProvider(
            String getGroupName, Set<Role> visibleTo, List<SearchResult> results)
            implements SearchProvider {
        @Override
        public List<SearchResult> search(String keyword) {
            return results;
        }
    }
}
