package com.movie.bookMyShow.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CredentialsRequest {
    private String username;
    private String password;
}
