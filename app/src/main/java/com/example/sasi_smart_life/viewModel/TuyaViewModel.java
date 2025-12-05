package com.example.sasi_smart_life.viewModel;

import android.content.Context;

import androidx.compose.runtime.MutableState;
import androidx.compose.runtime.SnapshotStateKt;
import androidx.compose.runtime.State;
import androidx.lifecycle.ViewModel;

import com.example.sasi_smart_life.SasiState;
import com.thingclips.smart.android.user.api.ILoginCallback;
import com.thingclips.smart.android.user.api.ILogoutCallback;
import com.thingclips.smart.android.user.api.IRegisterCallback;
import com.thingclips.smart.android.user.bean.User;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.home.sdk.api.IThingHomeChangeListener;
import com.thingclips.smart.home.sdk.bean.HomeBean;
import com.thingclips.smart.home.sdk.bean.RoomBean;
import com.thingclips.smart.home.sdk.builder.ActivatorBuilder;
import com.thingclips.smart.home.sdk.callback.IThingGetHomeListCallback;
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback;
import com.thingclips.smart.home.sdk.callback.IThingRoomResultCallback;
import com.thingclips.smart.sdk.api.IDevListener;
import com.thingclips.smart.sdk.api.IResultCallback;
import com.thingclips.smart.sdk.api.IThingActivator;
import com.thingclips.smart.sdk.api.IThingActivatorGetToken;
import com.thingclips.smart.sdk.api.IThingSmartActivatorListener;
import com.thingclips.smart.sdk.bean.DeviceBean;
import com.thingclips.smart.sdk.bean.GroupBean;
import com.thingclips.smart.sdk.enums.ActivatorModelEnum;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TuyaViewModel extends ViewModel {
    private final MutableState<SasiState> _uiState = SnapshotStateKt.mutableStateOf(new SasiState(), SnapshotStateKt.structuralEqualityPolicy());
    private final List<String> registeredDeviceIds = new ArrayList<>();
    private IThingActivator mThingActivator;


    // --- Listener Internal ---
    private final IThingHomeChangeListener homeChangeListener = new IThingHomeChangeListener() {
        @Override
        public void onHomeAdded(long homeId) {
            updateState(_uiState.getValue().withInfo("New home detected, refreshing..."));
            getHomeData();
        }

        @Override
        public void onHomeInvite(long homeId, String homeName) {
            updateState(_uiState.getValue().withInfo("Anda diundang ke home: " + homeName));
        }

        @Override
        public void onHomeRemoved(long homeId) {
            updateState(_uiState.getValue().withInfo("A home was removed, refreshing..."));
            getHomeData();
        }

        @Override
        public void onHomeInfoChanged(long homeId) {
            updateState(_uiState.getValue().withInfo("Home info changed, refreshing..."));
            getHomeData();
        }

        @Override
        public void onSharedDeviceList(List<DeviceBean> sharedDeviceList) {
            updateState(_uiState.getValue().withInfo("Shared device list changed, refreshing..."));
            getHomeData();
        }

        @Override
        public void onSharedGroupList(List<GroupBean> sharedGroupList) {
            updateState(_uiState.getValue().withInfo("Shared group list updated, refreshing..."));
            getHomeData();
        }

        @Override
        public void onServerConnectSuccess() {
            getHomeData();
        }
    };

    private final IDevListener devListener = new IDevListener() {
        @Override
        public void onDpUpdate(String devId, String dpStr) {
            updateState(_uiState.getValue().withInfo("Device " + devId + " updated."));
            Long homeId = _uiState.getValue().getCurrentHomeId();
            if(homeId != null) {
                getHomeDetail(homeId);
            }
        }

        @Override
        public void onRemoved(String devId) {
            updateState(_uiState.getValue().withInfo("Device " + devId + " removed."));
            getHomeData();
        }

        @Override
        public void onStatusChanged(String devId, boolean online) {
            updateState(_uiState.getValue().withInfo("Device " + devId + " is now " + (online ? "online" : "offline")));
            Long homeId = _uiState.getValue().getCurrentHomeId();
            if(homeId != null) {
                getHomeDetail(homeId);
            }
        }

        @Override
        public void onNetworkStatusChanged(String devId, boolean status) {
            updateState(_uiState.getValue().withInfo("Network for " + devId + " changed."));
            Long homeId = _uiState.getValue().getCurrentHomeId();
            if(homeId != null) {
                getHomeDetail(homeId);
            }
        }

        @Override
        public void onDevInfoUpdate(String devId) {
            updateState(_uiState.getValue().withInfo("Info for " + devId + " updated."));
            Long homeId = _uiState.getValue().getCurrentHomeId();
            if(homeId != null) {
                getHomeDetail(homeId);
            }
        }
    };
    // --- End Listener Internal ---

    public TuyaViewModel() {
        ThingHomeSdk.getHomeManagerInstance().registerThingHomeChangeListener(homeChangeListener);
        if (ThingHomeSdk.getUserInstance().isLogin()) {
            updateState(new SasiState(true, new ArrayList<>(), new ArrayList<>(), null, null, "Welcome back!", null, null, SasiState.PairingStep.IDLE));
            getHomeData();
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        ThingHomeSdk.getHomeManagerInstance().unRegisterThingHomeChangeListener(homeChangeListener);
        unregisterAllDeviceListeners();
        stopPairing();
    }

    private void unregisterAllDeviceListeners() {
        for (String devId : registeredDeviceIds) {
            ThingHomeSdk.newDeviceInstance(devId).unRegisterDevListener();
        }
        registeredDeviceIds.clear();
    }

    public State<SasiState> getUiState() {
        return _uiState;
    }

    private void updateState(SasiState newState) {
        _uiState.setValue(newState);
    }

    public void clearMessages() {
        updateState(_uiState.getValue().clearMessages());
    }


// LOGIKA LOGIN
    public void kirimKodeVerifikasi(String email) {
        ThingHomeSdk.getUserInstance().sendVerifyCodeWithUserName(email, "", "62", 1, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                updateState(_uiState.getValue().withError("Verification Code Error: " + error));
            }

            @Override
            public void onSuccess() {
                updateState(_uiState.getValue().withInfo("Verification code sent successfully."));
            }
        });
    }

    public void daftar(String email, String password, String verificationCode) {
        ThingHomeSdk.getUserInstance().registerAccountWithEmail("62", email, password, verificationCode, new IRegisterCallback() {
            @Override
            public void onSuccess(User user) {
                updateState(_uiState.getValue().asLoggedIn(true, "Registered and logged in successfully."));
                getHomeData();
            }

            @Override
            public void onError(String code, String error) {
                updateState(_uiState.getValue().withError("Registration Error: " + error));
            }
        });
    }

    public void login(String email, String password) {
        ThingHomeSdk.getUserInstance().loginWithEmail("62", email, password, new ILoginCallback() {
            @Override
            public void onSuccess(User user) {
                updateState(_uiState.getValue().asLoggedIn(true, "Logged in successfully."));
                getHomeData();
            }

            @Override
            public void onError(String code, String error) {
                updateState(_uiState.getValue().withError("Login Error: " + error));
            }
        });
    }

    public void logout() {
        ThingHomeSdk.getUserInstance().logout(new ILogoutCallback() {
            @Override
            public void onError(String code, String error) {
                updateState(_uiState.getValue().asLoggedIn(false, "Server logout failed, logged out locally."));
            }

            @Override
            public void onSuccess() {
                updateState(_uiState.getValue().asLoggedIn(false, "You have been logged out."));
            }
        });
    }
// END LOGIKA LOGIN

// LOGIKA HOME
    public void createHome() {

        ThingHomeSdk.getHomeManagerInstance().createHome("My Home", 0.0, 0.0,"" , null, new IThingHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean bean) {
                getHomeData();
            }
            @Override
            public void onError(String errorCode, String errorMsg) {
                updateState(_uiState.getValue().withError("Gagal membuat home otomatis : " + errorMsg));
            }
        });
    }

    public void getHomeData() {
        ThingHomeSdk.getHomeManagerInstance().queryHomeList(new IThingGetHomeListCallback() {
            @Override
            public void onSuccess(List<HomeBean> homeBeans) {
                updateState(_uiState.getValue().withHomeList(homeBeans));
                if (homeBeans.isEmpty()) {
                    createHome();
                } else {
                    long homeId = homeBeans.get(0).getHomeId();
                    getHomeDetail(homeId);
                }
            }
            @Override
            public void onError(String errorCode, String error) {
                updateState(_uiState.getValue().withError(error));
            }
        });
    }

    public void getHomeDetail(long homeId) {
        ThingHomeSdk.newHomeInstance(homeId).getHomeDetail(new IThingHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean bean) {
                updateState(_uiState.getValue().withHomeDetail(bean));

                // Daftarkan listener untuk setiap perangkat
                unregisterAllDeviceListeners();
                for (DeviceBean device : bean.getDeviceList()) {
                    String devId = device.getDevId();
                    ThingHomeSdk.newDeviceInstance(devId).registerDevListener(devListener);
                    registeredDeviceIds.add(devId);
                }
            }

            @Override
            public void onError(String errorCode, String error) {
                updateState(_uiState.getValue().withError("Gagal mendapatkan detail home: " + error));
            }
        });
    }
// END LOGIKA HOME

// LOGIKA PAIRING
    public void pairingDevice(Context context, String ssid, String password) {
        Long homeId = _uiState.getValue().getCurrentHomeId();
        if (homeId == null) {
            updateState(_uiState.getValue().withError("No home selected to pair device."));
            return;
        }

        updateState(_uiState.getValue().withPairingStep(SasiState.PairingStep.GETTING_TOKEN));

        ThingHomeSdk.getActivatorInstance().getActivatorToken(homeId,
                new IThingActivatorGetToken() {
                    @Override
                    public void onSuccess(String token) {
                        updateState(_uiState.getValue().withPairingStep(SasiState.PairingStep.SCANNING));
                        easyPairing(context, ssid, password, token);
                    }

                    @Override
                    public void onFailure(String s, String s1) {
                        updateState(_uiState.getValue().withError("Failed to get pairing token: " + s1).withPairingStep(SasiState.PairingStep.ERROR));
                    }
                });
    }

    private void easyPairing(Context context, String ssid, String password, String token) {
        ActivatorBuilder builder = new ActivatorBuilder()
                .setSsid(ssid)
                .setContext(context)
                .setPassword(password)
                .setActivatorModel(ActivatorModelEnum.THING_EZ)
                .setTimeOut(100)
                .setToken(token)
                .setListener(new IThingSmartActivatorListener() {
                    @Override
                    public void onError(String errorCode, String errorMsg) {
                        updateState(_uiState.getValue().withError("Pairing Error: " + errorMsg).withPairingStep(SasiState.PairingStep.ERROR));
                        stopPairing();
                    }

                    @Override
                    public void onActiveSuccess(DeviceBean devResp) {
                        updateState(_uiState.getValue().withInfo("Device '" + devResp.getName() + "' paired successfully. Refreshing...").withPairingStep(SasiState.PairingStep.SUCCESS));
                        getHomeData();
                    }

                    @Override
                    public void onStep(String step, Object data) {
                        if ("device_find".equals(step)) {
                            updateState(_uiState.getValue().withPairingStep(SasiState.PairingStep.CONNECTING));
                        } else {
                            updateState(_uiState.getValue().withInfo("Pairing step: " + step));
                        }
                    }
                });

        mThingActivator = ThingHomeSdk.getActivatorInstance().newMultiActivator(builder);
        if (mThingActivator != null) {
            mThingActivator.start();
        }
    }

    public void stopPairing() {
        if (mThingActivator != null) {
            mThingActivator.stop();
            mThingActivator.onDestroy();
            mThingActivator = null;
            updateState(_uiState.getValue().withInfo("Pairing stopped.").withPairingStep(SasiState.PairingStep.IDLE));
        }
    }
// END LOGIKA PAIRING

// LOGIKA DEVICE
    public void unlinkDevice(String devId) {
        ThingHomeSdk.newDeviceInstance(devId).removeDevice(new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                updateState(_uiState.getValue().withError("Failed to unlink device: " + error));
            }

            @Override
            public void onSuccess() {
                updateState(_uiState.getValue().withInfo("Device unlinked successfully."));
            }
        });
    }

    public void renameDevice(String devId, String newName) {
        ThingHomeSdk.newDeviceInstance(devId).renameDevice(newName, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                updateState(_uiState.getValue().withError("Failed to rename device: " + error));
            }

            @Override
            public void onSuccess() {
                updateState(_uiState.getValue().withInfo("Device renamed successfully."));
            }
        });
    }
// END LOGIKA DEVICE

// LOGIKA ROOM
    public void addRoom(String name) {
        Long homeId = _uiState.getValue().getCurrentHomeId();
        if (homeId == null) {
            updateState(_uiState.getValue().withError("No home selected to add room."));
            return;
        }

        ThingHomeSdk.newHomeInstance(homeId).addRoom(name, new IThingRoomResultCallback() {
            @Override
            public void onSuccess(RoomBean bean) {
                updateState(_uiState.getValue().withInfo("Room '" + name + "' added successfully."));
                getHomeDetail(homeId);
            }
            @Override
            public void onError(String errorCode, String errorMsg) {
                updateState(_uiState.getValue().withError("Failed to add room: " + errorMsg));
            }
        });
    }

    public void removeRoom(long roomId) {
        Long homeId = _uiState.getValue().getCurrentHomeId();
        if (homeId == null) {
            updateState(_uiState.getValue().withError("No home selected to remove room."));
            return;
        }
        ThingHomeSdk.newHomeInstance(homeId).removeRoom(roomId, new IResultCallback() {
            @Override
            public void onSuccess() {
                updateState(_uiState.getValue().withInfo("Room removed successfully."));
                getHomeDetail(homeId);
            }
            @Override
            public void onError(String code, String error) {
                updateState(_uiState.getValue().withError("Failed to remove room: " + error));
            }
        });
    }

    public void renameRoom(long roomId, String newName) {
        ThingHomeSdk.newRoomInstance(roomId).updateRoom(newName, new IResultCallback() {
            @Override
            public void onSuccess() {
                updateState(_uiState.getValue().withInfo("Room renamed successfully."));
                Long homeId = _uiState.getValue().getCurrentHomeId();
                if (homeId != null) {
                    getHomeDetail(homeId);
                }
            }
            @Override
            public void onError(String code, String error) {
                updateState(_uiState.getValue().withError("Failed to rename room: " + error));
            }
        });
    }

    public void sortRoom(List<Long> roomIds) {
        Long homeId = _uiState.getValue().getCurrentHomeId();
        if (homeId == null) {
            updateState(_uiState.getValue().withError("No home selected to sort rooms."));
            return;
        }
        ThingHomeSdk.newHomeInstance(homeId).sortRoom(roomIds, new IResultCallback() {
            @Override
            public void onSuccess() {
                updateState(_uiState.getValue().withInfo("Rooms sorted successfully."));
                getHomeDetail(homeId);
            }
            @Override
            public void onError(String code, String error) {
                updateState(_uiState.getValue().withError("Failed to sort rooms: " + error));
            }
        });
    }

    public void addDeviceToRoom(long roomId, String devId) {
        ThingHomeSdk.newRoomInstance(roomId).addDevice(devId, new IResultCallback() {
            @Override
            public void onSuccess() {
                updateState(_uiState.getValue().withInfo("Device added to room."));
                Long homeId = _uiState.getValue().getCurrentHomeId();
                if (homeId != null) {
                    getHomeDetail(homeId);
                }
            }
            @Override
            public void onError(String code, String error) {
                updateState(_uiState.getValue().withError("Failed to add device to room: " + error));
            }
        });
    }

    public void removeDeviceFromRoom(long roomId, String devId) {
        ThingHomeSdk.newRoomInstance(roomId).removeDevice(devId, new IResultCallback() {
            @Override
            public void onSuccess() {
                updateState(_uiState.getValue().withInfo("Device removed from room."));
                Long homeId = _uiState.getValue().getCurrentHomeId();
                if (homeId != null) {
                    getHomeDetail(homeId);
                }
            }
            @Override
            public void onError(String code, String error) {
                updateState(_uiState.getValue().withError("Failed to remove device from room: " + error));
            }
        });
    }

    public void updateRoomIcon(long roomId, File file) {
        ThingHomeSdk.newRoomInstance(roomId).updateIcon(file, new IResultCallback() {
            @Override
            public void onSuccess() {
                updateState(_uiState.getValue().withInfo("Room icon updated."));
                Long homeId = _uiState.getValue().getCurrentHomeId();
                if (homeId != null) {
                    getHomeDetail(homeId);
                }
            }

            @Override
            public void onError(String code, String error) {
                updateState(_uiState.getValue().withError("Failed to update room icon: " + error));
            }
        });
    }

    /**
     * Synchronously queries room information for a given device ID.
     * This method does not update the UI state directly.
     * @param devId The device ID.
     * @return RoomBean object or null if not found.
     */
    public RoomBean getDeviceRoomBean(String devId) {
        return ThingHomeSdk.getDataInstance().getDeviceRoomBean(devId);
    }
// END LOGIKA ROOM
}
