import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IUserReservations } from '../user-reservations.model';
import { UserReservationsService } from '../service/user-reservations.service';

@Component({
  templateUrl: './user-reservations-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class UserReservationsDeleteDialogComponent {
  userReservations?: IUserReservations;

  protected userReservationsService = inject(UserReservationsService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.userReservationsService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
