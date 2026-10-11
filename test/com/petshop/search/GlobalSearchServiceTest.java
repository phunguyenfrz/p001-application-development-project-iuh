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
    void returnsOnlyGroupsVisibleToTheCurrentRoleInProviderOrder() {
        SearchProvider customers = new StubProvider(
                "Khách hàng", Set.of(Role.BAN_HANG, Role.CHAM_SOC),
                new SearchResult("Khách hàng", "KH-1", "Minh Anh", "0900000000",
                        Screen.CUSTOMERS));
        SearchProvider pets = new StubProvider(
                "Thú cưng", Set.of(Role.CHAM_SOC),
                new SearchResult("Thú cưng", "TC-1", "Milo", "Khách hàng: Minh Anh",
                        Screen.PETS));
        GlobalSearchService service = new GlobalSearchService(List.of(customers, pets));

        assertEquals(List.of("Khách hàng"), service.search("minh", Role.BAN_HANG)
                .stream().map(SearchResult::groupName).toList());
        assertEquals(List.of("Khách hàng", "Thú cưng"), service.search("minh", Role.CHAM_SOC)
                .stream().map(SearchResult::groupName).toList());
        assertEquals(List.of(), service.search(" ", Role.CHAM_SOC));
    }

    private record StubProvider(
            String getGroupName, Set<Role> visibleTo, SearchResult result) implements SearchProvider {
        @Override
        public List<SearchResult> search(String keyword) {
            return List.of(result);
        }
    }
}
