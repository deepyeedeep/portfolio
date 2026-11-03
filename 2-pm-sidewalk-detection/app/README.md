# app: 안드로이드 앱

장치와 BLE로 연결해 노면 판별 결과를 받고, 위치 정보로 이동 상태를 판단해 서버에 기록하는 앱입니다.

| 파일 | 역할 |
|---|---|
| `navigation/MainNavigation.kt` | 권한 요청, 화면 전환 (시작 → 연결 준비 → 연결됨) |
| `viewmodel/KickboardConnectionViewModel.kt` | BLE, 위치, 소켓 세 Repository의 상태를 StateFlow로 모아 화면에 전달. 보도 주행·차도 반납 시 서버 전송 |
| `connection/BLERepo.kt` | 기기 이름으로 스캔 → GATT 연결 → notify 구독. 받은 결과가 연속으로 같을 때만 노면 상태를 바꿈 |
| `connection/LocationRepo.kt` | 최근 속도 평균으로 정지(0.91 m/s 미만) / 보행(1.6 m/s 미만) / 주행 판단 |
| `connection/SocketRepo.kt` | TCP 소켓으로 JSON 전송 (입출력 전용 디스패처에서 실행) |
| `screens/` | Jetpack Compose 화면 |

## 빌드 전에

- 소스만 정리한 폴더라 Gradle 래퍼와 루트 빌드 파일은 들어 있지 않습니다. Android Studio에서 새 Compose 프로젝트를 만든 뒤 `app/src`를 옮겨 넣는 방식으로 빌드합니다.
- 글꼴(`res/font/`)과 배경·로고 이미지(`res/drawable/background*.png`, `logo*.png`)는 라이선스를 확인하지 못해 뺐습니다. 같은 이름의 리소스를 넣어야 빌드됩니다.
- `SocketRepo.kt`의 서버 주소는 시연 때 쓴 TCP 터널 주소를 지우고 예시 값으로 바꿨습니다.
