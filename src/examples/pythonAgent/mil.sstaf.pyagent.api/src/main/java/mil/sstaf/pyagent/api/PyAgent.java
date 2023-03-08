/**
 * Template for the API for a custom Feature.
 *
 * Features must specify an interface that other Features can use to access
 * capabilities directly. The API must extend the type of Feature that will
 * be implemented. Specifically a simple Feature, a Handler or an Agent.
 *
 * Features are free to specify an arbitrary interface. However, any custom
 * types must also be included in the API module.
 *
 * Other features will specify this API interface as the type for the target
 * of a @Requires linkage.
 *
 */
package mil.sstaf.pyagent.api;

import mil.sstaf.core.features.Agent;

import java.util.List;

public interface PyAgent extends Agent {

    int countLetters(List<String> args);

}
