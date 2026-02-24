package com.campaignworkbench.ide;

import java.io.File;

/**
 * @param title Title of the file choose dialog
 * @param defaultFolder Default folder to go to when the dialog opens
 * @param description Description of the file extensions.
 * @param extension Default file extension to use when opening/saving files
 */
public record FileChooserConfig(
        String title,
        File defaultFolder,
        String description,
        String extension
) {}