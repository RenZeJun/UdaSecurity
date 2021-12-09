package cpm.udacity.catpoint.security.service;


import com.udacity.catpoint.image.service.ImageService;
import cpm.udacity.catpoint.security.data.*;
import cpm.udacity.catpoint.security.application.StatusListener;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.mockito.quality.Strictness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.extension.ExtendWith;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
//@MockitoSettings(strictness = Strictness.LENIENT)
public class SecurityServiceTest {


    private Sensor sensor;

    @Mock
    private StatusListener statusListener;

    @Mock
    private SecurityService securityService;

    @Mock
    private ImageService imageService;

    @Mock
    private SecurityRepository securityRepository;

    @BeforeEach
    void setUp(){
        securityService = new SecurityService(securityRepository, imageService);
        sensor = new Sensor("sensor", SensorType.WINDOW);

    }


    //test 1
    @Test
    void ifAlarmArmed_andSensorActivated_changeAlarmToPending(){
        when(securityService.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityService.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(sensor,true);
        verify(securityRepository, Mockito.times(1)).setAlarmStatus(AlarmStatus.PENDING_ALARM);

    }

    //test 2
    @Test
    void ifAlarmArmed_andSensorActivated_andSystemPending_setAlarmToOn(){
        when(securityService.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor,true);
        verify(securityRepository, Mockito.times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }


    //test 3
    @Test
    void ifAlarmIsPending_andSensorInactivated_returnToNoAlarm(){
        when(securityService.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        sensor.setActive(true);
        securityService.changeSensorActivationStatus(sensor, false);
        verify(securityRepository,times(1)).setAlarmStatus(eq(AlarmStatus.NO_ALARM));
    }


    //test 4
    /*@ParameterizedTest
    @ValueSource(booleans = {true, false})*/
    @Test
    void ifAlarmIsActive_changeSensorState_shouldNotAffectAlarmState(){
        when(securityService.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        securityService.changeSensorActivationStatus(sensor, false);
        verify(securityRepository,times(0)).setAlarmStatus(any(AlarmStatus.class));
    }


    //test 5
    @Test
    void ifSensorIsActivated_whileActive_andSystemPending_changeAlarmState(){
        sensor.setActive(true);
        when(securityService.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.changeSensorActivationStatus(sensor,true);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    //test 6
    @Test
    void ifSensorIsDeactivated_whileInactive_makeNoChangeToAlarmState(){
        sensor.setActive(false);
        when(securityService.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor,false);
        verify(securityRepository,times(0)).setAlarmStatus(any(AlarmStatus.class));
    }


    //test 7
    @Test
    void ifCameraImageContainsCat_whileSystemIsArmedHome_putSystemToAlarmStatus(){
        when(securityService.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(), ArgumentMatchers.anyFloat())).thenReturn(true);
        BufferedImage cat = new BufferedImage(200, 200,BufferedImage.TYPE_INT_RGB);
        securityService.processImage(cat);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    //test 8
    @Test
    void ifCameraImageDoesNotContainCat_changeToNoAlarm_asLongAsSensorsNotActive(){
        BufferedImage cat = new BufferedImage(200, 200,BufferedImage.TYPE_INT_RGB);
        when(imageService.imageContainsCat(any(),anyFloat())).thenReturn(false);
        sensor.setActive(false);
        securityService.processImage(cat);
        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    //test 9
    @Test
    void ifSystemDisarmed_setStatusToNoAlarm(){
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    //test 10
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME","ARMED_AWAY"})
    void ifSystemArmed_resetAllSensorToInactive(ArmingStatus armingStatus){
        Set<Sensor> sensors = new HashSet<>();
        for (int i = 0; i < 5; i++){
            sensors.add(new Sensor("sensor"+1,SensorType.WINDOW));
        }
        sensors.forEach(sensor->sensor.setActive(true));
        when(securityService.getSensors()).thenReturn(sensors);
        when(securityService.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        //when(securityService.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        securityService.setArmingStatus(armingStatus);
        securityService.getSensors().forEach(sensor -> {assertFalse(sensor.getActive());});
    }

    //test 11
    @Test
    void ifSystemArmedHome_whileCameraShowCat_setAlarmStatusToAlarm(){
        BufferedImage cat = new BufferedImage(200, 200,BufferedImage.TYPE_INT_RGB);
        when(securityService.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(),anyFloat())).thenReturn(true);
        securityService.processImage(cat);
        verify(securityRepository,times(1)).setAlarmStatus(any(AlarmStatus.class));
    }

    @Test //for coverage
    void testForStatusListener(){
        securityService.addStatusListener(statusListener);
        securityService.removeStatusListener(statusListener);
    }

    @Test //for coverage
    void testForSensor(){
        securityService.addSensor(sensor);
        securityService.removeSensor(sensor);
    }


    @Test
    void testForCoverage2(){
        when(securityService.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        sensor.setActive(false);
        securityService.changeSenorActivationCoverage();
        verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @Test
    void testForCoverage(){
        BufferedImage cat = new BufferedImage(200, 200,BufferedImage.TYPE_INT_RGB);
        when(imageService.imageContainsCat(any(),anyFloat())).thenReturn(true);
        securityService.processImage(cat);
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void testForCoverage3(){
        Set<Sensor> sensors = new HashSet<>();
        for (int i = 0; i < 5; i++){
            sensors.add(new Sensor("sensor"+1,SensorType.WINDOW));
        }
        sensors.forEach(sensor->sensor.setActive(true));
        when(securityService.getSensors()).thenReturn(sensors);

        BufferedImage cat = new BufferedImage(200, 200,BufferedImage.TYPE_INT_RGB);
        when(imageService.imageContainsCat(any(),anyFloat())).thenReturn(false);
        when(securityService.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.processImage(cat);
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        verify(securityRepository).setArmingStatus(any(ArmingStatus.class));
    }


}
