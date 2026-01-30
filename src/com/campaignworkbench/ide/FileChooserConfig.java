package com.campaignworkbench.ide;

import java.io.File;

public record FileChooserConfig(
        String title,
        File defaultFolder,
        String description,
        String extension
) {}