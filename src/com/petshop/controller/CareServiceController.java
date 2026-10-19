package com.petshop.controller;

import com.petshop.model.CareService;
import com.petshop.model.ServicePricing;
import com.petshop.service.CareServiceService;

import java.util.List;
import java.util.Optional;

/**
 * Controller điều phối giữa Giao diện người dùng (View) và Nghiệp vụ dịch vụ chăm sóc (Service).
 * Đảm bảo các View không truy vấn CSDL hay xử lý nghiệp vụ trực tiếp.
 */
public class CareServiceController {

    private final CareServiceService careServiceService;

    public CareServiceController() {
        this(new CareServiceService());
    }

    public CareServiceController(CareServiceService careServiceService) {
        this.careServiceService = careServiceService;
    }

    public List<CareService> searchCareServices(String keyword, Boolean activeFilter) {
        return careServiceService.searchCareServices(keyword, activeFilter);
    }

    public List<CareService> getAllCareServices() {
        return careServiceService.getAllCareServices();
    }

    public List<CareService> getActiveCareServices() {
        return careServiceService.getActiveCareServices();
    }

    public Optional<CareService> getServiceById(String maDichVu) {
        return careServiceService.getServiceById(maDichVu);
    }

    public boolean saveCareService(CareService service, boolean isCreate) {
        return careServiceService.saveCareService(service, isCreate);
    }

    public boolean deactivateCareService(String maDichVu) {
        return careServiceService.deactivateCareService(maDichVu);
    }

    public boolean activateCareService(String maDichVu) {
        return careServiceService.activateCareService(maDichVu);
    }

    public String generateNextServiceId() {
        return careServiceService.generateNextServiceId();
    }

    public List<ServicePricing> searchServicePricings(String keyword, String sizeFilter) {
        return careServiceService.searchServicePricings(keyword, sizeFilter);
    }

    public boolean saveServicePricing(ServicePricing pricing, boolean isCreate) {
        return careServiceService.saveServicePricing(pricing, isCreate);
    }

    public String generateNextPricingId() {
        return careServiceService.generateNextPricingId();
    }
}

