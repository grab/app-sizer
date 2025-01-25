```mermaid
flowchart TD
  A(generateApkDebug)
  B(generateArchiveDepDebug)
  C(appSizeAnalysisDebug)
  D(apkSizeAnalysisDebug)


  C --> A
  C --> B
  D --> B
```
