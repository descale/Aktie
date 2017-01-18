package aktie.net;

import java.util.logging.Level;
import java.util.logging.Logger;

import aktie.GenericProcessor;
import aktie.crypto.Utils;
import aktie.data.CObj;
import aktie.data.HH2Session;
import aktie.index.CObjList;
import aktie.index.Index;
import aktie.sequences.SubSequence;
import aktie.spam.SpamTool;
import aktie.user.IdentityManager;
import aktie.utils.DigestValidator;
import aktie.utils.SubscriptionValidator;

public class InSubProcessor extends GenericProcessor
{

    Logger log = Logger.getLogger ( "aktie" );

    private HH2Session session;
    private Index index;
    private DigestValidator validator;
    private SubscriptionValidator subvalidator;
    private ConnectionThread conThread;
    private CObj ConId;
    private IdentityManager identManager;

    public InSubProcessor ( HH2Session s, GetSendData2 cm, Index i, SpamTool st, IdentityManager im, ConnectionThread ct )
    {
        session = s;
        conThread = ct;
        index = i;
        identManager = im;
        ConId = conThread.getLocalDestination().getIdentity();
        validator = new DigestValidator ( index, st );
        subvalidator = new SubscriptionValidator ( index );
    }

    @Override
    public boolean process ( CObj b )
    {
        String type = b.getType();

        if ( CObj.SUBSCRIPTION.equals ( type ) )
        {
            //Check if it's valid and new
            if ( validator.valid ( b ) )
            {
                Long seqnum = b.getNumber ( CObj.SEQNUM );
                String creatorid = b.getString ( CObj.CREATOR );
                String comid = b.getString ( CObj.COMMUNITYID );

                if ( seqnum != null && creatorid != null && comid != null )
                {
                    String id = Utils.mergeIds ( comid, creatorid );

                    //The merged id should already be set, so check to make sure,
                    //if not, then we can't just change it because the signature would
                    //be bad.
                    if ( id.equals ( b.getId() ) )
                    {

                        boolean update = false;
                        //We already made this getSubscription method before we
                        //decided to do the merged id thing, just go with it.
                        CObj co = index.getSubscription ( comid, creatorid );

                        //Note, if we already have a subscription object, then the
                        //identity must be ok to subscribe to the community and
                        //we can just check the new sequence number is greater
                        if ( co != null )
                        {
                            //If the sequence number is less than we have just
                            //discard.
                            if ( seqnum > co.getNumber ( CObj.SEQNUM ) )
                            {
                                log.info ( "Newer version found!" );
                                update = true;
                            }

                        }

                        else
                        {
                            //We need to see if this user can subscribe.
                            if ( subvalidator.canSubscribe ( comid, creatorid ) )
                            {
                                log.info ( "New subscription, can subscribe." );
                                update = true;
                            }

                        }

                        try
                        {

                            SubSequence seq = new SubSequence ( session );
                            seq.setId ( creatorid );
                            seq.updateSequence ( b );

                            if ( update )
                            {
                                //Set the rank of the post based on the rank of the
                                //user
                                CObj idty = index.getIdentity ( creatorid );

                                if ( idty != null )
                                {
                                    Long rnk = idty.getPrivateNumber ( CObj.PRV_USER_RANK );

                                    if ( rnk != null )
                                    {
                                        b.pushPrivateNumber ( CObj.PRV_USER_RANK, rnk );
                                    }

                                }

                                long gseq = identManager.getGlobalSequenceNumber ( ConId.getId(), true );
                                b.pushPrivateNumber ( CObj.getGlobalSeq ( ConId.getId() ), gseq );

                                if ( log.isLoggable ( Level.INFO ) )
                                {
                                    log.info ( "SUB RECEIVED: ME: " + conThread.getLocalDestination().getIdentity().getId() +
                                               " FROM: " + conThread.getEndDestination().getId() + " DIG: " +
                                               b.getDig() + " SQ: " + gseq );
                                }

                                index.index ( b );
                                conThread.update ( b );

                                if ( "true".equals ( b.getString ( CObj.SUBSCRIBED ) ) )
                                {
                                    CObjList clst = index.getSubscriptions ( comid, null );

                                    for ( int c = 0; c < clst.size(); c++ )
                                    {
                                        CObj sl = clst.get ( c );
                                        String creator = sl.getString ( CObj.CREATOR );

                                        if ( creator != null )
                                        {
                                            identManager.updateGlobalSequenceNumber ( creator,
                                                    false, 0, false, 0, true, 0 );
                                        }

                                    }

                                    clst.close();
                                }

                            }

                        }

                        catch ( Exception e )
                        {
                            e.printStackTrace();

                        }


                    }

                }

            }

            return true;
        }

        return false;
    }

}
