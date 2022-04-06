///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package edu.pitt.isp.sverchkov.egvz;
//
//import edu.pitt.isp.sverchkov.bn.BayesNet;
//import edu.pitt.isp.sverchkov.collections.Pair;
//import edu.pitt.isp.sverchkov.smile.SMILEBayesNet;
//import edu.uci.ics.jung.algorithms.layout.*;
//import edu.uci.ics.jung.algorithms.layout.SpringLayout;
//import edu.uci.ics.jung.graph.DirectedGraph;
//import edu.uci.ics.jung.graph.DirectedSparseGraph;
//import edu.uci.ics.jung.visualization.BasicVisualizationServer;
//import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
//import edu.uci.ics.jung.visualization.RenderContext;
//import edu.uci.ics.jung.visualization.VisualizationViewer;
//import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
//import edu.uci.ics.jung.visualization.control.EditingModalGraphMouse;
//import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
//import edu.uci.ics.jung.visualization.decorators.EdgeShape;
//import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
//import edu.uci.ics.jung.visualization.util.Animator;
//
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.io.File;
//import java.io.IOException;
//import javax.swing.*;
//import javax.swing.event.TreeWillExpandListener;
//import javax.swing.tree.DefaultMutableTreeNode;
//import javax.swing.tree.DefaultTreeModel;
//
///**
// *
// * @author yus24
// */
//public class EDGrVisualization {
//
//    private static final String
//            TITLE = "Explanation of Differences across Groups",
//            ERROROF = "Error opening file";
//
//    private final JFrame frame;
//    private final JTree tree;
//    private GraphAdapter<String,String> ga;
//
//    /**
//     * @param args the command line arguments
//     */
//    public static void main(String[] args) {
//        EDGrVisualization me = new EDGrVisualization();
//        me.run();
//    }
//
//    public EDGrVisualization(){
//        frame = new JFrame(TITLE);
//        tree = new JTree( new DefaultMutableTreeNode("No data loaded.") );
//    }
//
//    public void run(){
//
//        // Object creation:
//        final JMenuBar menuBar = new JMenuBar();
//        final JMenu fileMenu = new JMenu("File");
//        final JMenu graphMenu = new JMenu("Graph Mouse");
//        final JMenuItem menuItemOpen = initFileOpenItem();
//        //final JMenuItem menuItemLoadTest = initTestLoadItem();
//        final Pair<JPanel,JMenu> graphThings = makeBlankCanvas();
//        //final JPanel drawing = graphThings.first;
//        final JScrollPane treeView = new JScrollPane( tree );
//        final JSplitPane splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, graphThings.first, treeView );
//
//        // Frame behaviors
//        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//
//        // Connect objects
//        fileMenu.add(menuItemOpen);
//        graphMenu.add(graphThings.second);//menuItemLoadTest);
//        menuBar.add(fileMenu);
//        //menuBar.add(graphThings.second);
//        menuBar.add(graphMenu);
//        //menuBar.add(zoomMenu);
//        //menuBar.add(actionMenu);
//        frame.setJMenuBar(menuBar);
//        frame.add( splitPane, BorderLayout.CENTER );
//
//        // Sizing
//        treeView.setSize(new Dimension(200,400));
//
//        // Show frame
//        frame.pack();
//        frame.setVisible(true);
//    }
//
//    private Pair<JPanel,JMenu> makeBlankCanvas() {
//        DirectedGraph<GraphAdapter.Node<String,String>,Integer> g = new DirectedSparseGraph<>();
//        //new DAGLayout<>(g);//new KKLayout<>(g);//
//        SpringLayout<GraphAdapter.Node<String,String>,Integer> layout = new SpringLayout<>(g);
//        //layout.setRepulsionRange( ); // Default 100
//        layout.setForceMultiplier( 0.1 ); // Default 1/3
//        //layout.setSize(new Dimension(200,400));
//        VisualizationViewer<GraphAdapter.Node<String,String>,Integer> vv = new VisualizationViewer<>( layout );
//
//        DefaultModalGraphMouse<GraphAdapter.Node<String,String>,Integer> gm = new DefaultModalGraphMouse<>();
//        vv.setGraphMouse( gm );
//        gm.setMode(ModalGraphMouse.Mode.PICKING);
//        JMenu menu = gm.getModeMenu();
//        //menu.setText("X");
//
//        RenderContext<GraphAdapter.Node<String, String>, Integer> rc = vv.getRenderContext();
//        rc.setVertexLabelTransformer(new ToStringLabeller());
//        rc.setEdgeShapeTransformer(new EdgeShape.Line(g));
//        ga = new GraphAdapter<>(g, vv, layout);
//        GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
//
//        //gzsp.add(gm.getModeComboBox(),BorderLayout.SOUTH);
//
//        return new Pair<JPanel,JMenu>(gzsp,menu);
//    }
//
//    private JMenuItem initTestLoadItem() {
//        final JMenuItem item = new JMenuItem("Load Test");
//        item.addActionListener( new ActionListener(){
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                tree.setModel( new DefaultTreeModel( new DefaultMutableTreeNode("Test model loaded.") ) );
//            }
//        } );
//        return item;
//    }
//
//    private JMenuItem initFileOpenItem() {
//        final JMenuItem item = new JMenuItem("Open");
//
//        final FileDialog fd = new FileDialog( frame, frame.getTitle()+": Open", FileDialog.LOAD );
//        fd.setMultipleMode(false);
//
//        item.addActionListener( new ActionListener(){
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                fd.setVisible(true);
//                final String fileName = fd.getFile();
//                if( null != fileName ){
//
//                    final boolean idConversion = 0 == JOptionPane.showOptionDialog(
//                            frame,
//                            "Use SMILE ID conversion?",
//                            "ID conversion",
//                            JOptionPane.YES_NO_OPTION,
//                            JOptionPane.QUESTION_MESSAGE,
//                            null, //icon
//                            null, // options
//                            null // initialValue
//                    );
//
//                    try{
//                        final String filename = fd.getFile();
//                        final String directory = fd.getDirectory();
//                        final File file = new File(directory,filename);
//
//                        {
//                            final BayesNet<String,String> net = new SMILEBayesNet( file, idConversion );
//                            final Object[] nodes = new Object[net.size()];
//                            int i = 0;
//                            for( String node : net )
//                                nodes[i++] = node;
//
//                            final String contrast = (String) JOptionPane.showInputDialog(
//                                    frame,
//                                    "Select the contrast variable:",
//                                    "Pick Z",
//                                    JOptionPane.PLAIN_MESSAGE,
//                                    null,
//                                    nodes,
//                                    null
//                            );
//
//                            // Remove old objects
//                            //TreeExpansionListener[] oldTELs = tree.getTreeExpansionListeners();
//                            TreeWillExpandListener[] oldTWELs = tree.getTreeWillExpandListeners();
//                            for (TreeWillExpandListener old : oldTWELs)
//                                tree.removeTreeWillExpandListener( old );
//                            //for (TreeExpansionListener old : oldTELs)
//                            //    tree.removeTreeExpansionListener( old );
//
//
//                            EDGrTreeController<String,String> controller = new EDGrTreeController<>(file.getCanonicalPath(), net, contrast);
//                            new Thread(controller).start();
//                            ga.setNet( net );
//                            tree.setModel( controller.getModel() );
//                            tree.addTreeWillExpandListener( controller );
//                            tree.addTreeSelectionListener( controller.makeTSL(ga) );
//                        }
//                        // Read object
//                        //tree.setModel( new DefaultTreeModel( new DefaultMutableTreeNode("Pretended to open "+file.getCanonicalPath()) ) );
//                        /*try( ObjectInputStream in = new ObjectInputStream( new FileInputStream( file ) ) ){
//                            clearAndFillApplet( (BDDDResult<String>) in.readObject(), true );
//                        }*/
//
//                    }catch( IOException ex ){
//                        JOptionPane.showMessageDialog( frame, ex.getLocalizedMessage(), ERROROF, JOptionPane.ERROR_MESSAGE );
//                    }
//                }
//            }
//        });
//        return item;
//    }
//
//}
