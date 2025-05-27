import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { IUserReservations } from '../user-reservations.model';

@Component({
  selector: 'jhi-user-reservations-detail',
  templateUrl: './user-reservations-detail.component.html',
  imports: [SharedModule, RouterModule, FormatMediumDatetimePipe],
})
export class UserReservationsDetailComponent {
  userReservations = input<IUserReservations | null>(null);

  previousState(): void {
    window.history.back();
  }
}
