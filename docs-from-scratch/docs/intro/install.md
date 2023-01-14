---
title: "Install"
---

## MacOS

Grab the latest release with [homebrew][5]:

```bash
brew tap malinskiy/tap
brew install malinskiy/tap/marathon
```

## GitHub Releases

Grab the latest release from [GitHub Releases][1] page. Extract the archive into your apps folder and add the binary to your path using
local terminal session or using your profile file (.bashrc or equivalent), e.g.

```bash
unzip -d $DESTINATION marathon-X.X.X.zip
export PATH=$PATH:$DESTINATION/marathon-X.X.X/bin
```

[1]: https://github.com/MarathonLabs/marathon/releases
[2]: https://search.maven.org/
[4]: https://github.com/MarathonLabs/marathon/releases/latest
[5]: https://brew.sh/
