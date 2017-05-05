package wormguides.models.subscenegeometry;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

/**
 * {@link MeshView} for scene elements that also contains marker points where the line for callout notes could be drawn
 * from. These meshes are created by the {@link wormguides.loaders.GeometryLoader}.
 */
public class SceneElementMeshView extends MeshView {

    /**
     * Number of marker points to pick out from the vertices of the mesh view. As the number of mark points
     * increases, the line drawn to the callout note is able to get closer to the point on the mesh closest to it.
     **/
    private final static int NUMBER_OF_MARKER_POINTS = 16;

    /** The list of marker points **/
    private final List<double[]> markerCoordinates;

    /**
     * Class constructor
     *
     * @param triangleMesh
     *         the triangle mesh needed to create a {@link MeshView} object
     */
    public SceneElementMeshView(final TriangleMesh triangleMesh) {
        super(triangleMesh);
        markerCoordinates = new ArrayList<>();
    }

    /**
     * Picks out the number of marker points specified from a list of vertices
     *
     * @param vertices
     *         the list of vertices
     */
    public void pickOutMarkerPoints(final List<double[]> vertices) {
        final int interval = vertices.size() / NUMBER_OF_MARKER_POINTS;
        double[] vertex;
        for (int i = 0; i < vertices.size(); i++) {
            if (i % interval == 0) {
                vertex = vertices.get(i);
                markerCoordinates.add(new double[]{vertex[0], vertex[1], vertex[2]});
            }
        }
    }

    /**
     * @return the marker points (previously picked out upon loading the .obj file)
     */
    public List<double[]> getMarkerCoordinates() {
        return new ArrayList<>(markerCoordinates);
    }
}
