/**
 * <p>
 * This package contains code for handling the communication with the provider. The main functionality is:
 * </p>
 * <ul>
 *     <li>Connecting the adapter to the provider SSE endpoint</li>
 *     <li>Sending status back to the provider</li>
 *     <li>Sending the response back to the provider</li>
 * </ul>
 *
 * <p>
 * <b><i>There should be no need to change the code in this package</i></b>
 * <i>If you need to add more logic when verifying if the event can be handled it should be done
 * in the {@link no.fint.provider.adapter.event.EventStatusService#verifyEvent(no.fint.event.model.Event)}
 * method.</i>
 * </p>
 *
 */
package no.fint.provider.adapter;