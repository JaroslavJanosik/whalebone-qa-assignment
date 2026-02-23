package cz.whalebone.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class TestContext {

    private final String apiBaseUrl;
    private final String uiBaseUrl;

    /**
     * Populated by BaseUiTest#setUp only for UI tests.
     */
    @Setter
    private GUIContext gui;
}
