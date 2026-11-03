# Raspberry Pi 5에서 Hailo PCIe 드라이버 다시 설치하기

Raspberry Pi OS에는 Hailo PCIe 드라이버(`hailo_pci`)가 미리 들어 있습니다. 사용할 HailoRT 4.19.0과 드라이버 버전을 맞추기 위해, 기본 모듈을 제거하고 같은 버전의 드라이버 패키지를 다시 설치했습니다.

## 환경

- Raspberry Pi 5 (8 GB), Raspberry Pi OS
- 커널: `6.6.31+rpt-rpi-2712`
- Hailo-8L (M.2, PCIe)
- HailoRT 4.19.0 (Hailo Developer Zone에서 내려받음, 로그인 필요)
  - `hailort-pcie-driver_4.19.0_all.deb`
  - `hailort_4.19.0_arm64.deb`
  - `hailort-4.19.0-cp311-cp311-linux_aarch64.whl`

## 1. 미리 설치된 드라이버 제거

```bash
uname -r    # 6.6.31+rpt-rpi-2712
sudo rm /lib/modules/6.6.31+rpt-rpi-2712/kernel/drivers/media/pci/hailo/hailo_pci.ko.xz
```

## 2. 드라이버와 런타임 설치

```bash
sudo apt install bison libelf-dev dkms
sudo dpkg --install hailort-pcie-driver_4.19.0_all.deb
sudo dpkg --install hailort_4.19.0_arm64.deb
sudo reboot now
```

드라이버 패키지는 미리 컴파일된 모듈이 아니라 소스가 들어 있는 패키지(`_all`)입니다. 설치할 때 DKMS가 현재 커널에 맞춰 `hailo_pci` 모듈을 빌드하고, 커널이 업데이트되면 다시 빌드합니다. `dkms`, `libelf-dev`, `bison`은 이 빌드에 필요한 도구입니다.

## 3. 모듈 파라미터를 지정해 다시 적재

```bash
sudo modprobe -r hailo_pci
sudo modprobe hailo_pci force_desc_page_size=4096
```

## 4. 확인

```bash
lspci | grep Co-processor   # PCIe 장치 인식
lsmod | grep hailo_pci      # 드라이버 적재
dmesg | grep hailo          # 펌웨어 로드
```

## 5. Python API 설치

picamera2는 시스템 패키지라 일반 가상환경에서는 import되지 않습니다. 시스템 패키지를 함께 쓰는 옵션으로 가상환경을 만듭니다.

```bash
python -m venv hailo_env --system-site-packages
. ./hailo_env/bin/activate
pip install hailort-4.19.0-cp311-cp311-linux_aarch64.whl
pip install opencv-python
```
