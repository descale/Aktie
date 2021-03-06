package aktie.net;

import java.util.logging.Logger;

import aktie.GenericProcessor;
import aktie.data.CObj;
import aktie.index.Index;

public class ReqDigProcessor extends GenericProcessor
{
    Logger log = Logger.getLogger ( "aktie" );

    private ConnectionThread conThread;
    private Index index;

    public ReqDigProcessor ( Index i, ConnectionThread ct )
    {
        conThread = ct;
        index = i;
    }

    private void log ( String msg )
    {
        if ( conThread.getEndDestination() != null )
        {
            log.info ( "ME: " + conThread.getLocalDestination().getIdentity().getId() +
                       " FROM: " + conThread.getEndDestination().getId() + " " + msg );
        }

    }

    @Override
    public boolean process ( CObj b )
    {
        if ( CObj.CON_REQ_DIG.equals ( b.getType() ) )
        {
            String d = b.getDig();
            //Get the object with that digest.
            CObj rid = conThread.getEndDestination();

            if ( d != null && rid != null )
            {
                CObj o = index.getByDig ( d );
                log ( "GET DIG: " + d + " obj: " + o );

                if ( o != null )
                {
                    if ( CObj.POST.equals ( o.getType() )  ||
                            CObj.HASFILE.equals ( o.getType() ) )
                    {
                        String comid = o.getString ( CObj.COMMUNITYID );

                        if ( comid != null )
                        {
                            if ( conThread.getSubs().contains ( comid ) )
                            {
                                log ( "SND PST/HAS: " + d );
                                conThread.enqueue ( o );
                            }

                        }

                    }

                    if ( CObj.SUBSCRIPTION.equals ( o.getType() ) )
                    {
                        String comid = o.getString ( CObj.COMMUNITYID );

                        if ( conThread.getMemberships().contains ( comid ) )
                        {
                            //Just send it if they're already members
                            log ( "SND PRV SUB: " + d );
                            conThread.enqueue ( o );
                        }

                        else
                        {
                            CObj com = index.getCommunity ( comid );

                            if ( com != null )
                            {
                                String pp = com.getString ( CObj.SCOPE );

                                if ( CObj.SCOPE_PUBLIC.equals ( pp ) )
                                {
                                    log ( "SND PUB SUB: " + d );
                                    conThread.enqueue ( o );
                                }

                            }

                        }

                    }

                    if ( CObj.MEMBERSHIP.equals ( o.getType() ) ||
                            CObj.PRIVIDENTIFIER.equals ( o.getType() ) ||
                            CObj.PRIVMESSAGE.equals ( o.getType() ) ||
                            CObj.COMMUNITY.equals ( o.getType() ) ||
                            CObj.IDENTITY.equals ( o.getType() ) ||
                            CObj.SPAMEXCEPTION.equals ( o.getType() ) )
                    {
                        log ( "SND PBLC: " + d );
                        conThread.enqueue ( o );
                    }

                }

            }

            return true;
        }

        return false;
    }

}
