package org.phoebus.hdf.image.display;

import org.phoebus.applications.email.EmailEntry;
import org.phoebus.framework.adapter.AdapterFactory;
import org.phoebus.logbook.AttachmentImpl;
import org.phoebus.logbook.LogEntry;
import org.phoebus.logbook.LogEntryImpl;
import org.phoebus.logbook.LogbookPreferences;
import org.phoebus.ui.javafx.Screenshot;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static org.phoebus.logbook.LogEntryImpl.LogEntryBuilder.log;

public class SelectionAdapterFactory implements AdapterFactory {

    private static final List<? extends Class> adaptableTypes = Arrays.asList(EmailEntry.class, LogEntry.class);
    private static final Logger logger = Logger.getLogger(SelectionAdapterFactory.class.getName());

    @Override
    public Class getAdaptableObject() {
        return SelectionInfo.class;
    }

    @Override
    public List<? extends Class> getAdapterList() {
        return adaptableTypes;
    }

    @Override
    public <T> Optional<T> adapt(Object adaptableObject, Class<T> adapterType) {
        SelectionInfo selectionInfo = ((SelectionInfo) adaptableObject);

        if (adapterType.isAssignableFrom(EmailEntry.class))
        {
            EmailEntry emailEntry = new EmailEntry();

            StringBuffer title = new StringBuffer();
            title.append("Display Screenshot for : " + selectionInfo.getName());
            emailEntry.setSubject(title.toString());
            emailEntry.setBody(selectionInfo.getName());
            emailEntry.setImages(List.of(selectionInfo.getImage()));

            return Optional.of(adapterType.cast(emailEntry));
        }
        else if (adapterType.isAssignableFrom(LogEntry.class))
        {
            LogEntryImpl.LogEntryBuilder log = log().title(LogbookPreferences.auto_title ?
                            "Display Screenshot for : " + selectionInfo.getName() : "")
                    .appendDescription(selectionInfo.getName());
            try
            {
                final File image_file = selectionInfo.getImage() == null ? null : new Screenshot(selectionInfo.getImage()).writeToTempfile("image");
                log.attach(AttachmentImpl.of(image_file));
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            return Optional.of(adapterType.cast(log.build()));
        }
        return Optional.ofNullable(null);
    }
}
