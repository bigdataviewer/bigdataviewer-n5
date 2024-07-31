[![Build Status](https://github.com/bigdataviewer/bigdataviewer-n5/actions/workflows/build.yml/badge.svg)](https://github.com/bigdataviewer/bigdataviewer-n5/actions/workflows/build.yml)

# bigdataviewer-n5

[![](https://github.com/bigdataviewer/bigdataviewer-n5/actions/workflows/build-main.yml/badge.svg)](https://github.com/bigdataviewer/bigdataviewer-n5/actions/workflows/build-main.yml)
[![developer chat](https://img.shields.io/badge/zulip-join_chat-brightgreen.svg)](https://imagesc.zulipchat.com/#narrow/stream/327326-BigDataViewer)

This adds a BigDataViewer ImgLoader backend for the format `bdv.n5.cloud`.

For example something like this XML would use it:
```xml
<SpimData version="0.2">
  <BasePath type="relative">.</BasePath>
  <SequenceDescription>
    <ImageLoader format="bdv.n5.cloud" version="0.1">
      <n5 type="relative">dataset.n5</n5>
    </ImageLoader>
    ...
  </SequenceDescription>
  ...
</SpimData>
```

In contrast to the `bdv.n5` format in bigdataviewer-core, the `bdv.n5.cloud`
backend uses `N5Factory` to construct a `N5Reader`. The `bdv.n5` format is (was)
hard-coded to use a filesystem reader and N5 format, so it will not work for N5
stored on AWS S3 or Google Cloud, and/or in Zarr format.

However, the handler for `bdv.n5` will now try to forward to `bdv.n5.cloud` if
opening with the hard-coded reader fails. Hopefully, there should be no need to
specify `bdv.n5.cloud` in the XML. It should be sufficient to put the
`bigdataviewer-n5` artefact on the classpath.

The `bdv.n5.cloud` format is probably temporary, to be removed when we figure
out how to do this properly. Avoid using it explicitly if possible.