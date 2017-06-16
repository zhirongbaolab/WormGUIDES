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
    private String id;
    private String style;
    private String innerHTML;

    // for type checking
    private boolean isContainer;
    private boolean hasId;
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
     * container node with no id - e.g. head, body, ul
     *
     * @param tag
     *         the type of node
     */
    public HTMLNode(String tag) {
        this.tag = tag;

        this.isContainer = true;
        this.hasId = false;
        this.isImage = false;
        this.isStyle = false;
        this.isButton = false;
        this.isScript = false;

        this.id = null;
        this.style = null;
        this.innerHTML = null;
        this.imgSrc = null;
        this.onclick = null;
        this.script = null;

        this.children = new ArrayList<>();
    }

    /**
     * Container node with id - e.g. <div>
     *
     * @param tag
     *         the type of node
     * @param ID
     *         the id corresponding to this node
     * @param style
     *         the style associated with this nodes
     */
    public HTMLNode(String tag, String ID, String style) {
        this.tag = tag;
        this.id = ID;
        this.style = style;

        this.isContainer = true;
        this.hasId = true;
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
     *         the id corresponding to this node
     * @param style
     *         the style associated with this node
     * @param innerHTML
     *         the text inside the tags
     */
    public HTMLNode(String tag, String ID, String style, String innerHTML) {
        this.tag = tag;
        this.id = ID;
        this.style = style;
        this.innerHTML = innerHTML;

        this.hasId = true;
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
        this.hasId = false;
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
            this.id = type; // we'll use the id var for type="text/css"
        }

        this.isStyle = true;
        this.isContainer = false;
        this.hasId = false;
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
     *         the id associated with this node
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
        this.id = ID;
        this.style = style;
        this.innerHTML = buttonText;

        this.isButton = button;
        this.isContainer = false;
        this.hasId = true;
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
        this.hasId = false;
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
         * TODO - container without id --> <head> - container with id --> div -
		 * format with children
		 */

        if (node == null) {
            return null;
        }

        StringBuilder nodeStr = new StringBuilder();
        if (node.isContainer()) { // e.g. <head>, <div>
            if (!node.hasID()) { // e.g. <head>
                nodeStr = new StringBuilder()
                        .append(NEW_LINE)
                        .append("<")
                        .append(node.getTag())
                        .append(">");
            } else { // e.g. <div>
                nodeStr = new StringBuilder()
                        .append(NEW_LINE)
                        .append("<")
                        .append(node.getTag())
                        .append(" id=\"")
                        .append(node.getId())
                        .append("\">");
            }

            // add children to node
            if (node.hasChildren()) {
                for (HTMLNode n : node.getChildren()) {
                    nodeStr.append(formatNode(n));
                }
            }

            if (!node.getTag().equals("br")) {
                nodeStr.append(NEW_LINE)
                        .append("</")
                        .append(node.getTag())
                        .append(">");
            }

        } else if (!node.isContainer() && !node.isImage() && !node.isButton() && !node.isScript()) { // e.g.
            // <p
            // id...
            if (!node.getId().equals("")) {
                nodeStr = new StringBuilder()
                        .append(NEW_LINE)
                        .append("<")
                        .append(node.getTag())
                        .append(" id=\"")
                        .append(node.getId())
                        .append("\">")
                        .append(NEW_LINE)
                        .append(node.getInnerHTML())
                        .append(NEW_LINE)
                        .append("</")
                        .append(node.getTag())
                        .append(">");
            } else {
                nodeStr = new StringBuilder()
                        .append(NEW_LINE)
                        .append("<")
                        .append(node.getTag())
                        .append(">")
                        .append(NEW_LINE)
                        .append(node.getInnerHTML())
                        .append(NEW_LINE)
                        .append("</")
                        .append(node.getTag())
                        .append(">");
            }

        } else if (node.isImage()) { // e.g. <img id...
            nodeStr = new StringBuilder()
                    .append(NEW_LINE)
                    .append("<")
                    .append(node.getTag())
                    .append(" src=\"")
                    .append(node.getImgSrc())
                    .append("\" alt=\"")
                    .append(node.getImgSrc())
                    .append("\">");
        } else if (node.isStyle()) {
            nodeStr = new StringBuilder()
                    .append(NEW_LINE)
                    .append("<")
                    .append(node.getTag())
                    .append(" type=\"")
                    .append(node.getId())
                    .append("\">")
                    .append(NEW_LINE)
                    .append(NEW_LINE)
                    .append(node.getStyle())
                    .append(NEW_LINE)
                    .append("</")
                    .append(node.getTag())
                    .append(">");
        } else if (node.isButton()) { // using imgSrc for onlick and innerHTML
            // for button text
            nodeStr = new StringBuilder()
                    .append(NEW_LINE)
                    .append("<")
                    .append(node.getTag())
                    .append(" onclick=\"")
                    .append(node.getOnclick())
                    .append("()\"")
                    .append(" id=\"")
                    .append(node.getId())
                    .append("\">")
                    .append(node.innerHTML)
                    .append("</")
                    .append(node.getTag())
                    .append(">");
        } else if (node.isScript()) {
            nodeStr = new StringBuilder()
                    .append(NEW_LINE)
                    .append("<")
                    .append(node.getTag())
                    .append(">")
                    .append(NEW_LINE)
                    .append(node.getScript())
                    .append(NEW_LINE)
                    .append("</")
                    .append(node.getTag())
                    .append(">");
        }
        return nodeStr.toString();
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
        final StringBuilder scriptStringBruilder = new StringBuilder();
        scriptStringBruilder.append(functionName)
                .append(NEW_LINE)
                .append("    if (document.getElementById('")
                .append(this.getId())
                .append("').innerHTML == \"+\") {")
                .append(NEW_LINE)
                .append("        document.getElementById('")
                .append(this.getId())
                .append("').innerHTML = \"-\";")
                .append(NEW_LINE)
                .append("        document.getElementById('")
                .append(divToCollapseID)
                .append("').style.height = '20%'; ")
                .append(NEW_LINE)
                .append("        document.getElementById('")
                .append(divToCollapseID)
                .append("').style.visibility = 'visible'; ")
                .append(NEW_LINE)
                .append("    } else {")
                .append(NEW_LINE)
                .append("        document.getElementById('")
                .append(this.getId())
                .append("').innerHTML = \"+\";")
                .append(NEW_LINE)
                .append("        document.getElementById(\"")
                .append(divToCollapseID)
                .append("\").style.height = '0px'; ")
                .append(NEW_LINE)
                .append("        document.getElementById('")
                .append(divToCollapseID)
                .append("').style.visibility = 'hidden'; ")
                .append(NEW_LINE)
                .append("    }")
                .append(NEW_LINE)
                .append("}");
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
        return new HTMLNode("script", scriptStringBruilder.toString(), true);
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
        final StringBuilder scriptStringBuilder = new StringBuilder();
        scriptStringBuilder.append("function homologuesCollapse() {")
                .append(NEW_LINE)
                .append("if (document.getElementById('homologuesCollapseButton').innerHTML == \"+\") {")
                .append(NEW_LINE)
                .append("document.getElementById('homologuesCollapseButton').innerHTML = \"-\";")
                .append(NEW_LINE)
                .append("document.getElementById('homologues').style.height = '20%';")
                .append(NEW_LINE)
                .append("document.getElementById('homologues').style.visibility = 'visible'; ")
                .append(NEW_LINE)
                .append("document.getElementById('homologuesLR').style.height = '20%';")
                .append(NEW_LINE)
                .append("document.getElementById('homologuesLR').style.visibility = 'visible';")
                .append(NEW_LINE)
                .append("document.getElementById('homologuesOther').style.height = '20%';")
                .append(NEW_LINE)
                .append("document.getElementById('homologuesOther').style.visibility = 'visible';")
                .append(NEW_LINE)
                .append("} else {")
                .append(NEW_LINE)
                .append("document.getElementById('homologuesCollapseButton').innerHTML = \"+\";")
                .append(NEW_LINE)
                .append("document.getElementById('homologues').style.height = '0px'; ")
                .append(NEW_LINE)
                .append("document.getElementById('homologues').style.visibility = 'hidden';")
                .append(NEW_LINE)
                .append("document.getElementById('homologuesLR').style.height = '0px';")
                .append(NEW_LINE)
                .append("document.getElementById('homologuesLR').style.visibility = 'hidden';")
                .append(NEW_LINE)
                .append("document.getElementById('homologuesOther').style.height = '0px';")
                .append(NEW_LINE)
                .append("document.getElementById('homologuesOther').style.visibility = 'hidden';")
                .append(NEW_LINE)
                .append("}")
                .append(NEW_LINE)
                .append("}");
        return new HTMLNode("script", scriptStringBuilder.toString(), true);
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

    public String getId() {
        if (this.id != null) {
            return this.id;
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
        return this.hasId;
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
