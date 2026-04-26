# HeraTalk

LAN-based push-to-talk voice chat for Android. No servers, no accounts, no internet required.

[![Build](https://github.com/relexx/heratalk/actions/workflows/build.yml/badge.svg)](https://github.com/relexx/heratalk/actions/workflows/build.yml)
[![License: BSD-3-Clause](https://img.shields.io/badge/License-BSD_3--Clause-blue.svg)](LICENSE)

## Features

- Peer-to-peer mesh over Wi-Fi, zero infrastructure
- Encrypted audio (SRTP + Noise protocol)
- Broadcast channels and 1:1 direct calls
- Push-to-talk or VOX (voice-activated)
- Works under hostile AP conditions (client isolation, multicast filtering, jitter)

## Requirements

- Android 10 (API 29) or higher
- Wi-Fi network shared by all participants

## Building

See [docs/devcontainer.md](docs/devcontainer.md) for the recommended dev container setup.
For a local build:

    ./gradlew assembleDebug

## Documentation

- [Architecture](docs/architecture.md)
- [Requirements](docs/requirements.md)
- [Security model](docs/security.md)
- [Release plan](docs/releases.md)
- [Current project state](docs/project-state.md)
- [Architecture Decision Records](docs/adrs/)

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

BSD 3-Clause License. See [LICENSE](LICENSE).

Copyright © 2026 relexx (https://relexx.de)
