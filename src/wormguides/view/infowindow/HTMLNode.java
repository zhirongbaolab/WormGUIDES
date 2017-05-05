/*
 * Bao Lab 2016
 */

package wormguides.view.infowindow;

import java.util.ArrayList;

/**
 * Nodes for the DOM tree
 */
public class HTMLNode {

    private final static String NEW_LINE = "\n";

    private String tag;
    private String ID;
    private String style;
    private String innerHTML;

    // for type checking
    private boolean isContainer;
    private boolean hasID;
    private boolean isImage;
    private boolean isStyle;
    private boolean isButton;
    private boolean isScript;

    // button vars
    private String onclick;

    // script vars
    private String script;

    // image vars
    private String imgSrc;

    private ArrayList<HTMLNode> children;

    /**
     * container node with no ID - e.g. head, body, ul
     *
     * @param tag
     *         the type of node
     */
    public HTMLNode(String tag) {
        this.tag = tag;

        this.isContainer = true;
        this.hasID = false;
        this.isImage = false;
        this.isStyle = false;
        this.isButton = false;
        this.isScript = false;

        this.ID = null;
        this.style = null;
        this.innerHTML = null;
        this.imgSrc = null;
        this.onclick = null;
        this.script = null;

        this.children = new ArrayList<>();
    }

    /**
     * Container node with ID - e.g. <div>
     *
     * @param tag
     *         the type of node
     * @param ID
     *         the ID corresponding to this node
     * @param style
     *         the style associated with this nodes
     */
    public HTMLNode(String tag, String ID, String style) {
        this.tag = tag;
        this.ID = ID;
        this.style = style;

        this.isContainer = true;
        this.hasID = true;
        this.isImage = false;
        this.isStyle = false;
        this.isButton = false;
        this.isScript = false;

        this.innerHTML = null;
        this.imgSrc = null;
        this.onclick = null;
        this.script = null;

        this.children = new ArrayList<>();
    }

    /**
     * inner node - e.g. <p>
     *
     * @param tag
     *         the type of node
     * @param ID
     *         the ID corresponding to this node
     * @param style
     *         the style associated with this node
     * @param innerHTML
     *         the text inside the tags
     */
    public HTMLNode(String tag, String ID, String style, String innerHTML) {
        this.tag = tag;
        this.ID = ID;
        this.style = style;
        this.innerHTML = innerHTML;

        this.hasID = true;
        this.isContainer = false;
        this.isImage = false;
        this.isStyle = false;
        this.isButton = false;
        this.isScript = false;

        this.imgSrc = null;
        this.onclick = null;
        this.script = null;
        children = null;
    }

    /**
     * inner node - e.g. <p>
     *
     * @param imgSrc
     *         the URL of the image
     * @param isImage
     *         the flag that this is an image (true)
     */
    public HTMLNode(String imgSrc, boolean isImage) {
        this.tag = "img";
        this.imgSrc = imgSrc;

        this.isImage = isImage;
        this.isContainer = false;
        this.hasID = false;
        this.isStyle = false;
        this.isButton = false;
        this.isScript = false;

        this.onclick = null;
        this.script = null;
        this.innerHTML = null;
        this.children = null;
    }

    /**
     * The style node
     *
     * @param style
     *         the formatted string of style for the DOM
     * @param type
     *         - usually set to text/css
     */
    public HTMLNode(String style, String type) {
        this.tag = "style";
        this.innerHTML = style;

        if (type.equals("text/css")) {
            this.ID = type; // we'll use the ID var for type="text/css"
        }

        this.isStyle = true;
        this.isContainer = false;
        this.hasID = false;
        this.isImage = false;
        this.isButton = false;
        this.isScript = false;

        this.imgSrc = null;
        this.style = null;
        this.onclick = null;
        this.script = null;
        this.children = null;
    }

    /**
     * A button node for collapsing sections
     *
     * @param tag
     *         the type of node
     * @param onclick
     *         the function to call onclick
     * @param ID
     *         the ID associated with this node
     * @param style
     *         the style associated with this node
     * @param buttonText
     *         the text to go inside of the button
     * @param button
     *         the flag indentifying this as a button (true)
     */
    public HTMLNode(String tag, String onclick, String ID, String style, String buttonText, boolean button) {
        this.tag = tag;
        this.onclick = onclick;
        this.ID = ID;
        this.style = style;
        this.innerHTML = buttonText;

        this.isButton = button;
        this.isContainer = false;
        this.hasID = true;
        this.isImage = false;
        this.isStyle = false;
        this.isScript = false;

        this.imgSrc = null;
        this.script = null;
        this.children = null;
    }

    /**
     * A javascript node
     *
     * @param tag
     *         the type of node
     * @param script
     *         the js function/script
     * @param isScript
     *         the flag identifying this as a script (true)
     */
    public HTMLNode(String tag, String script, boolean isScript) {
        this.tag = tag;
        this.script = script;

        this.isScript = isScript;
        this.isContainer = false;
        this.hasID = false;
        this.isImage = false;
        this.isStyle = false;

        this.imgSrc = null;
        this.innerHTML = null;
        this.children = null;

    }

    public void addChild(HTMLNode child) {
        if (children == null) {
            return;
        }

        if (child != null) {
            this.children.add(child);
        }
    }

    public String formatNode() {
        return formatNode(this);
    }

	/*
     * TODO - add remove child method
	 */

    /**
     * Formats a given node and its children into a string.
     *
     * @param node
     *         node to be formatted
     *
     * @return string representation of the formatted node
     */
    private String formatNode(HTMLNode node) {
        /*
         * TODO - container without ID --> <head> - container with ID --> div -
		 * format with children
		 */

        if (node == null) {
            return null;
        }

        String nodeStr = "";
        if (node.isContainer()) { // e.g. <head>, <div>

            if (!node.hasID()) { // e.g. <head>
                nodeStr = NEW_LINE + "<" + node.getTag() + ">";
            } else { // e.g. <div>
                nodeStr = NEW_LINE + "<" + node.getTag() + " id=\"" + node.getID() + "\">";
            }

            // add children to node
            if (node.hasChildren()) {
                for (HTMLNode n : node.getChildren()) {
                    nodeStr += formatNode(n);
                }
            }

            if (!node.getTag().equals("br")) {
                nodeStr += (NEW_LINE + "</" + node.getTag() + ">");
            }

        } else if (!node.isContainer() && !node.isImage() && !node.isButton() && !node.isScript()) { // e.g.
            // <p
            // id...
            if (!node.getID().equals("")) {
                nodeStr = NEW_LINE + "<" + node.getTag() + " id=\"" + node.getID() + "\">" + NEW_LINE
                        + node.getInnerHTML() + NEW_LINE + "</" + node.getTag() + ">";
            } else {
                nodeStr = NEW_LINE + "<" + node.getTag() + ">" + NEW_LINE + node.getInnerHTML() + NEW_LINE + "</"
                        + node.getTag() + ">";
            }

        } else if (node.isImage()) { // e.g. <img id...
            nodeStr = NEW_LINE + "<" + node.getTag() + " src=\"" + node.getImgSrc() + "\" alt=\"" + node.getImgSrc()
                    + "\">";
        } else if (node.isStyle()) {
            nodeStr = NEW_LINE + "<" + node.getTag() + " type=\"" + node.getID() + "\">" + NEW_LINE + NEW_LINE
                    + node.getStyle() + NEW_LINE + "</" + node.getTag() + ">";
        } else if (node.isButton()) { // using imgSrc for onlick and innerHTML
            // for button text
            nodeStr = NEW_LINE + "<" + node.getTag() + " onclick=\"" + node.getOnclick() + "()\"" + " id=\""
                    + node.getID() + "\">" + node.innerHTML + "</" + node.getTag() + ">";
        } else if (node.isScript()) {
            nodeStr =
                    NEW_LINE + "<" + node.getTag() + ">" + NEW_LINE + node.getScript() + NEW_LINE + "</" + node.getTag()
                            + ">";
        }

        return nodeStr;
    }

    /**
     * Makes a script node to collapse a section on click of a button
     *
     * @return the script node with the collapsing function
     */
    public HTMLNode makeCollapseButtonScript() {
        if (!this.isButton()) {
            return null;
        }

        String functionName = "function " + this.getOnclick() + "() {";

        String divToCollapseID = this.getOnclick().substring(0, this.getOnclick().indexOf("Collapse"));
        String script = functionName + NEW_LINE + "    if (document.getElementById('" + this.getID()
                + "').innerHTML == \"+\") {" + NEW_LINE + "        document.getElementById('" + this.getID()
                + "').innerHTML = \"-\";" + NEW_LINE + "        document.getElementById('" + divToCollapseID
                + "').style.height = '20%'; " + NEW_LINE + "        document.getElementById('" + divToCollapseID
                + "').style.visibility = 'visible'; " + NEW_LINE + "    } else {" + NEW_LINE
                + "        document.getElementById('" + this.getID() + "').innerHTML = \"+\";" + NEW_LINE
                + "        document.getElementById(\"" + divToCollapseID + "\").style.height = '0px'; " + NEW_LINE
                + "        document.getElementById('" + divToCollapseID + "').style.visibility = 'hidden'; " + NEW_LINE
                + "    }" + NEW_LINE + "}";

		/*
		 * 
		 * function function________Collapse() { if
		 * (document.getElementById('functionCollapseButton').innerHTML == "+")
		 * { document.getElementById('functionCollapseButton').innerHTML = "-";
		 * document.getElementById("functionWORMATLAS").style.height = '20%';
		 * document.getElementById('functionWORMATLAS').style.visibility =
		 * 'visible'; } else {
		 * document.getElementById('functionCollapseButton').innerHTML = "+";
		 * document.getElementById("functionWORMATLAS").style.height = '0px';
		 * document.getElementById('functionWORMATLAS').style.visibility =
		 * 'hidden'; } }
		 */
        return new HTMLNode("script", script, true);
    }

    /**
     * Collapse function specific for homologues because it's made of two ULs
     *
     * @return the script node with the collapsing function
     */
    public HTMLNode makeHomologuesCollapseButtonScript() {
        if (!this.isButton()) {
            return null;
        }

        String script = "function homologuesCollapse() {" + NEW_LINE
                + "if (document.getElementById('homologuesCollapseButton').innerHTML == \"+\") {" + NEW_LINE
                + "document.getElementById('homologuesCollapseButton').innerHTML = \"-\";" + NEW_LINE
                + "document.getElementById('homologues').style.height = '20%';" + NEW_LINE
                + "document.getElementById('homologues').style.visibility = 'visible'; " + NEW_LINE
                + "document.getElementById('homologuesLR').style.height = '20%';" + NEW_LINE
                + "document.getElementById('homologuesLR').style.visibility = 'visible';" + NEW_LINE
                + "document.getElementById('homologuesOther').style.height = '20%';" + NEW_LINE
                + "document.getElementById('homologuesOther').style.visibility = 'visible';" + NEW_LINE + "} else {"
                + NEW_LINE + "document.getElementById('homologuesCollapseButton').innerHTML = \"+\";" + NEW_LINE
                + "document.getElementById('homologues').style.height = '0px'; " + NEW_LINE
                + "document.getElementById('homologues').style.visibility = 'hidden';" + NEW_LINE
                + "document.getElementById('homologuesLR').style.height = '0px';" + NEW_LINE
                + "document.getElementById('homologuesLR').style.visibility = 'hidden';" + NEW_LINE
                + "document.getElementById('homologuesOther').style.height = '0px';" + NEW_LINE
                + "document.getElementById('homologuesOther').style.visibility = 'hidden';" + NEW_LINE + "}" + NEW_LINE
                + "}";

        return new HTMLNode("script", script, true);
    }

    /**
     * The script that handles callbacks to Java when a link is clicked --> allows WormGUIDES to target external browser
     *
     * @return the script node with the callback function
     */
    public HTMLNode addLinkHandlerScript() {
        String script = "function handleLink(element) {" + NEW_LINE + "app.handleLink(element.name);" + NEW_LINE + "}";
        return new HTMLNode("script", script, true);

    }

    /**
     * @return the script node with the callback to Java for generating a new tab on click
     */
    public HTMLNode handleWiringPartnerClickScript() {
        String script = "function handleWiringPartnerClick(element) {" + NEW_LINE
                + "app.handleWiringPartnerClick(element.innerHTML);" + NEW_LINE + "}";

        return new HTMLNode("script", script, true);
    }

    /**
     * @return the script node with the callback to Java for viewing a clicked cell in the cell theater
     */
    public HTMLNode viewInCellTheaterScript() {
        String script = "function viewInCellTheater(element) {" + NEW_LINE + "app.viewInCellTheater(element.name);"
                + NEW_LINE + "}";

        return new HTMLNode("script", script, true);
    }

    public HTMLNode handleAmphidClickScript() {
        String script = "function handleAmphidClick() {" + NEW_LINE + "app.handleAmphidClick();"
                + NEW_LINE + "}";

        return new HTMLNode("script", script, true);
    }

    public String getTag() {
        if (this.tag != null) {
            return this.tag;
        }
        return "";
    }

    public String getID() {
        if (this.ID != null) {
            return this.ID;
        }

        return "";
    }

    public String getStyle() {
        if (this.style != null) {
            return this.style;
        }

        if (this.isButton()) {
            System.out.println("BUTTON STYLE RETURNED NOTHING");
        }
        return "";
    }

    public String getInnerHTML() {
        if (this.innerHTML != null) {
            return this.innerHTML;
        }
        return "";
    }

    public String getOnclick() {
        if (this.onclick != null) {
            return this.onclick;
        }

        return "";
    }

    public String getImgSrc() {
        if (this.imgSrc != null) {
            return this.imgSrc;
        }

        return "";
    }

    public String getScript() {
        if (this.script != null) {
            return this.script;
        }

        return "";
    }

    public ArrayList<HTMLNode> getChildren() {
        if (this.hasChildren()) {
            return this.children;
        }
        return null;
    }

    public boolean hasChildren() {
        return children != null && this.children.size() > 0;
    }

    public boolean isContainer() {
        return this.isContainer;
    }

    public boolean hasID() {
        return this.hasID;
    }

    public boolean isImage() {
        return this.isImage;
    }

    public boolean isStyle() {
        return this.isStyle;
    }

    public boolean isButton() {
        return this.isButton;
    }

    public boolean isScript() {
        return this.isScript;
    }
}
