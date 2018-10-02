# stm32flash-util
Java based library to talk to stm32 UART bootloader following [AN3155](https://www.st.com/resource/en/application_note/cd00264342.pdf)

See [stm32flash-util](https://github.com/grevaillot/stm32flash-util) for usage.

## Notes:

  - Make sure that your UART interface is configured as specified in AN3155 - with proper parity setting.
  - Device table is taken from [stm32flash](https://sourceforge.net/projects/stm32flash/) project.

## Tested Devices:

  - Project has not been tested against much devices (basically only F0, F1, L0, L1 devices).

 - stm32f042x4, stm32f030xC, stm32f091xC
 - stm32f100x6, stm32f103x6
 - stm32l151xC
 - stm32l072xC
