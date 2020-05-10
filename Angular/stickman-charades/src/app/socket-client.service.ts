import { Injectable } from '@angular/core';
import { SocketClientState } from './SocketClientState';
import { Observable } from 'rxjs';
import { over, filter, first, switchMap } from '../assets/js/stomp.min.js';
import { Client, BehaviorSubject, StompSubscription, Message } from '../assets/js/stomp.min.js';
import * as SockJS from '../assets/js/sockjs.min.js';


@Injectable({
  providedIn: 'root'
})
export class SocketClientService {

  private client: Client;
  private state: BehaviorSubject<SocketClientState>;

  constructor() { 
    this.client = over(new SockJS("http://192.168.160.103:54880"));
    this.state = new BehaviorSubject<SocketClientState>(SocketClientState.ATTEMPTING);
    this.client.connect({}, () => {
      this.state.next(SocketClientState.CONNECTED);
    });
  }

  private connect(): Observable<Client> {
    return new Observable<Client>(observer => {
      this.state.pipe(filter(state => state === SocketClientState.CONNECTED)).subscribe(() => {
        observer.next(this.client);
      });
    });
  }

  onMessage(topic: string, handler = SocketClientService.jsonHandler): Observable<any> {
    return this.connect().pipe(first(), switchMap(client => {
      return new Observable<any>(observer => {
        const subscription: StompSubscription = client.subscribe(topic, message => {
          observer.next(handler(message.body));
        });
        return () => client.unsubscribe(subscription.id);
      });
    }));
  }

  static jsonHandler(message: Message): any {
    return JSON.parse(message.body);
  }
  
  static textHandler(message: Message): string {
    return message.body;
  }

}
