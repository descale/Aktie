package aktie.net;

import java.util.logging.Logger;

import aktie.GenericProcessor;
import aktie.data.CObj;
import aktie.index.Index;

public class InDigProcessor extends GenericProcessor
{

    Logger log = Logger.getLogger ( "aktie" );

    private ConnectionThread conThread;
    private Index index;

    public InDigProcessor ( ConnectionThread ct, Index i )
    {
        conThread = ct;
        index = i;
    }

    @Override
    public boolean process ( CObj b )
    {
        if ( CObj.OBJDIG.equals ( b.getType() ) )
        {
            String d = b.getDig();
            log.info ( "RCV DIG LIST: ME: " + conThread.getLocalDestination().getIdentity().getId() +
                       " FROM: " + conThread.getEndDestination().getId() + " DIG: " + d );

            if ( d != null )
            {
                CObj o = index.getByDig ( d );

                if ( o == null )
                {
                    conThread.addReqDig ( d );
                }

            }

            return true;
        }

        return false;
    }

}
