package com.woopaca.likeknu.service;

import com.woopaca.likeknu.Campus;
import com.woopaca.likeknu.controller.dto.device.request.CampusModificationRequest;
import com.woopaca.likeknu.controller.dto.device.request.ChangeNotificationRequest;
import com.woopaca.likeknu.controller.dto.device.request.DeviceRegistrationRequest;
import com.woopaca.likeknu.controller.dto.device.request.DeviceTokenRequest;
import com.woopaca.likeknu.entity.Device;
import com.woopaca.likeknu.exception.BusinessException;
import com.woopaca.likeknu.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public void registerDevice(DeviceRegistrationRequest deviceRequest) {
        String deviceId = deviceRequest.deviceId();
        Device device = deviceRepository.findById(deviceId)
                .orElseGet(deviceRequest::toEntity);

        device.update(deviceRequest.platform(), deviceRequest.campus(), deviceRequest.themeColor(),
                deviceRequest.favoriteCafeteria(), deviceRequest.modelName(), deviceRequest.osVersion(),
                deviceRequest.appVersion(), deviceRequest.expoPushToken());
        deviceRepository.save(device);
    }

    public void modifyCampusByDeviceId(CampusModificationRequest campusModificationRequest) {
        String deviceId = campusModificationRequest.deviceId();
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new BusinessException(String.format("deviceId: %s does not exist.", deviceId)));

        try {
            Campus campus = Campus.valueOf(campusModificationRequest.campus());
            device.setCampus(campus);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("Invalid campus");
        }
    }

    public void registerTokenByDevice(DeviceTokenRequest tokenRequest) {
        String deviceId = tokenRequest.deviceId();
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new BusinessException(String.format("Device not found! [%s]", deviceId)));
        device.updateExpoPushToken(tokenRequest.token());
    }

    @Transactional(readOnly = true)
    public boolean isTurnOnPushNotifications(String deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new BusinessException(String.format("Device not found! [%s]", deviceId)));
        return device.isTurnOnNotification();
    }

    public void changeDeviceNotifications(ChangeNotificationRequest request) {
        String deviceId = request.deviceId();
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new BusinessException(String.format("Device not found! [%s]", deviceId)));

        device.updateNotification(request.notification());
    }
}
