package aktie.gui;

import java.util.Iterator;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Table;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;

import aktie.data.CObj;
import aktie.gui.pm.PrivateMessageDialog;
import aktie.index.CObjList;

public class ShowPrivComDialog extends Dialog
{
    private Text searchTxt;
    private Table communityTable;
    private SWTApp app;
    private TableViewer communityTableViewer;
    private PrivateMessageDialog msgDialog;

    /**
        Create the dialog.
        @param parentShell
    */
    public ShowPrivComDialog ( Shell parentShell, SWTApp a )
    {
        super ( parentShell );
        setShellStyle ( getShellStyle() | SWT.RESIZE );
        app = a;
    }

    public void setMessageDialog ( PrivateMessageDialog m )
    {
        msgDialog = m;
    }

    @Override
    protected void configureShell ( Shell shell )
    {
        super.configureShell ( shell );
        shell.setText ( "Locked Communities" );
    }

    /**
        Create contents of the dialog.
        @param parent
    */
    @Override
    protected Control createDialogArea ( Composite parent )
    {
        Composite container = ( Composite ) super.createDialogArea ( parent );
        container.setLayout ( new GridLayout ( 2, false ) );

        Label lblCommunitiesYouAre = new Label ( container, SWT.NONE );
        lblCommunitiesYouAre.setText ( "Private communities you are not a member of (locked)." );
        new Label ( container, SWT.NONE );

        Label lblRequestAccessFrom = new Label ( container, SWT.NONE );
        lblRequestAccessFrom.setText ( "Request access from the creator. (Right click - Send Message)" );
        new Label ( container, SWT.NONE );

        searchTxt = new Text ( container, SWT.BORDER );
        searchTxt.setLayoutData ( new GridData ( SWT.FILL, SWT.CENTER, true, false, 1, 1 ) );
        searchTxt.addListener ( SWT.Traverse, new Listener()
        {
            @Override
            public void handleEvent ( Event event )
            {
                if ( event.detail == SWT.TRAVERSE_RETURN )
                {
                    doSearch();
                }

            }

        } );

        Button btnSearch = new Button ( container, SWT.NONE );
        btnSearch.setText ( "Search" );
        btnSearch.addSelectionListener ( new SelectionListener()
        {
            @Override
            public void widgetSelected ( SelectionEvent e )
            {
                doSearch ( );
            }

            @Override
            public void widgetDefaultSelected ( SelectionEvent e )
            {
            }

        } );

        communityTableViewer = new TableViewer ( container, SWT.BORDER | SWT.FULL_SELECTION );
        communityTable = communityTableViewer.getTable();
        communityTable.setLayoutData ( new GridData ( SWT.FILL, SWT.FILL, true, true, 1, 1 ) );
        communityTable.setHeaderVisible ( true );
        communityTable.setLinesVisible ( true );
        new Label ( container, SWT.NONE );
        communityTableViewer.setContentProvider ( new CObjListContentProvider() );

        Menu menu_5 = new Menu ( communityTable );
        communityTable.setMenu ( menu_5 );

        MenuItem newmsg = new MenuItem ( menu_5, SWT.NONE );
        newmsg.setText ( "Send Message" );
        newmsg.addSelectionListener ( new SelectionListener()
        {
            @Override
            public void widgetSelected ( SelectionEvent e )
            {
                if ( msgDialog != null )
                {
                    CObj selid = app.getSelectedIdentity();

                    if ( selid != null )
                    {
                        IStructuredSelection sel = ( IStructuredSelection ) communityTableViewer.getSelection();
                        @SuppressWarnings ( "rawtypes" )
                        Iterator i = sel.iterator();

                        if ( i.hasNext() )
                        {
                            CObjListArrayElement ae = ( CObjListArrayElement ) i.next();
                            CObj com = ae.getCObj();

                            if ( com != null )
                            {
                                String creator = com.getString ( CObj.CREATOR );

                                if ( creator != null )
                                {
                                    msgDialog.open ( selid.getId(), creator );
                                }

                            }

                        }

                    }

                }

            }

            @Override
            public void widgetDefaultSelected ( SelectionEvent e )
            {
            }

        } );


        TableViewerColumn col0 = new TableViewerColumn ( communityTableViewer, SWT.NONE );
        col0.getColumn().setText ( "Community" );
        col0.getColumn().setWidth ( 150 );
        col0.setLabelProvider ( new CObjListDisplayNameColumnLabelProvider() );
        col0.getColumn().addSelectionListener ( new SelectionListener()
        {

            @Override
            public void widgetSelected ( SelectionEvent e )
            {
                String ns = CObj.docPrivate ( CObj.PRV_DISPLAY_NAME );

                if ( ns.equals ( sortPostField1 ) )
                {
                    sortPostReverse = !sortPostReverse;
                }

                else
                {
                    sortPostField1 = ns;
                    sortPostReverse = false;
                    sortPostType1 = SortField.Type.STRING;
                }

                doSearch();
            }

            @Override
            public void widgetDefaultSelected ( SelectionEvent e )
            {
            }

        } );

        TableViewerColumn col1 = new TableViewerColumn ( communityTableViewer, SWT.NONE );
        col1.getColumn().setText ( "Creator" );
        col1.getColumn().setWidth ( 150 );
        col1.setLabelProvider ( new CObjListStringColumnLabelProvider ( CObj.CREATOR_NAME ) );
        col1.getColumn().addSelectionListener ( new SelectionListener()
        {

            @Override
            public void widgetSelected ( SelectionEvent e )
            {
                String ns = CObj.docString ( CObj.CREATOR_NAME );

                if ( ns.equals ( sortPostField1 ) )
                {
                    sortPostReverse = !sortPostReverse;
                }

                else
                {
                    sortPostField1 = ns;
                    sortPostReverse = false;
                    sortPostType1 = SortField.Type.STRING;
                }

                doSearch();

            }

            @Override
            public void widgetDefaultSelected ( SelectionEvent e )
            {
            }

        } );

        fillData();

        return container;
    }

    public int open()
    {
        fillData();
        return super.open();
    }

    private String sortPostField1;
    private boolean sortPostReverse;
    private SortField.Type sortPostType1;

    private void doSearch ()
    {
        String str = searchTxt.getText();
        doSearch ( str );
    }

    private void doSearch ( String str )
    {
        CObjList clst = ( CObjList ) communityTableViewer.getInput();
        Sort s = new Sort();

        if ( sortPostField1 != null )
        {
            SortField sf = new SortField ( sortPostField1, sortPostType1, sortPostReverse );
            s.setSort ( sf );
        }

        else
        {
            SortField sf = new SortField ( CObj.docPrivate ( CObj.PRV_DISPLAY_NAME ), SortField.Type.STRING, false );
            s.setSort ( sf );
        }

        CObjList nlst = app.getNode().getIndex().searchSemiPrivateCommunities ( str, s );
        communityTableViewer.setInput ( nlst );

        if ( clst != null )
        {
            clst.close();
        }

    }

    public void fillData()
    {
        if ( communityTableViewer != null && communityTable != null && !communityTable.isDisposed() )
        {
            doSearch ( null );
        }

    }

    /**
        Create contents of the button bar.
        @param parent
    */
    @Override
    protected void createButtonsForButtonBar ( Composite parent )
    {
        createButton ( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                       false );
        createButton ( parent, IDialogConstants.CANCEL_ID,
                       IDialogConstants.CANCEL_LABEL, false );
    }

    /**
        Return the initial size of the dialog.
    */
    @Override
    protected Point getInitialSize()
    {
        return new Point ( 450, 300 );
    }

    public Text getSearchTxt()
    {
        return searchTxt;
    }

    public Table getCommunityTable()
    {
        return communityTable;
    }

    public TableViewer getCommunityTableViewer()
    {
        return communityTableViewer;
    }

}
