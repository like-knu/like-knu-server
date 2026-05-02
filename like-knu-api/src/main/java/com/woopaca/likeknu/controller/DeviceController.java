package com.woopaca.likeknu.controller;

import com.woopaca.likeknu.controller.dto.base.ResponseDto;
import com.woopaca.likeknu.controller.dto.device.request.CampusModificationRequest;
import com.woopaca.likeknu.controller.dto.device.request.ChangeNotificationRequest;
import com.woopaca.likeknu.controller.dto.device.request.DeviceRegistrationRequest;
import com.woopaca.likeknu.controller.dto.device.request.DeviceTokenRequest;
import com.woopaca.likeknu.controller.dto.device.response.TurnOnNotificationResponse;
import com.woopaca.likeknu.service.DeviceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/devices")
@RestController
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping
    public ResponseDto<String> registerDevice(@RequestBody DeviceRegistrationRequest request) {
        deviceService.registerDevice(request);
        return ResponseDto.of("The device was successfully registered.");
    }

    @PutMapping("/campus")
    public ResponseDto<String> modifyCampus(@RequestBody CampusModificationRequest request) {
        deviceService.modifyCampusByDeviceId(request);
        return ResponseDto.of("Campus has been changed successfully.");
    }

    @PostMapping("/token")
    public ResponseDto<String> registerTokenByDevice(@RequestBody DeviceTokenRequest request) {
        deviceService.registerTokenByDevice(request);
        return ResponseDto.of("The token is well registered.");
    }

    @GetMapping("/notifications")
    public ResponseDto<TurnOnNotificationResponse> whetherDeviceTurnOnNotifications(
            @RequestParam("deviceId") String deviceId
    ) {
        boolean isTurnOn = deviceService.isTurnOnPushNotifications(deviceId);
        return ResponseDto.of(new TurnOnNotificationResponse(isTurnOn));
    }

    @PutMapping("/notifications")
    public ResponseDto<String> updateTurnOnDeviceNotifications(
            @RequestBody ChangeNotificationRequest request
    ) {
        deviceService.changeDeviceNotifications(request);
        return ResponseDto.of("Device push notification settings have been changed.");
    }
}
